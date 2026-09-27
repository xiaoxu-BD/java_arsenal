package org.xiaoxu.wecommail.mail;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xiaoxu.wecommail.common.EmailType;
import org.xiaoxu.wecommail.common.SendStatus;
import org.xiaoxu.wecommail.entity.CargoDeclareInfo;
import org.xiaoxu.wecommail.entity.ClientEmail;
import org.xiaoxu.wecommail.entity.EnterpriseWechatRecord;
import org.xiaoxu.wecommail.entity.MailSendDetail;
import org.xiaoxu.wecommail.entity.MailSendRecord;
import org.xiaoxu.wecommail.mapper.CargoDeclareInfoMapper;
import org.xiaoxu.wecommail.mapper.ClientEmailMapper;
import org.xiaoxu.wecommail.mapper.EnterpriseWechatRecordMapper;
import org.xiaoxu.wecommail.mapper.MailSendDetailMapper;
import org.xiaoxu.wecommail.mapper.MailSendRecordMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 单条发送记录的执行器:CAS 认领 → 查未成功发送消息 → 写明细/快照 → 发送 → 回写终态。
 * 多实例/多线程安全由 CAS(状态条件 UPDATE)保证,SMTP 调用失败/超时进入 FAIL 由调度器按退避重试。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailSendService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final MailSendRecordMapper recordMapper;
    private final MailSendDetailMapper detailMapper;
    private final EnterpriseWechatRecordMapper messageMapper;
    private final CargoDeclareInfoMapper configMapper;
    private final ClientEmailMapper emailMapper;
    private final MailSenderFacade mailSender;

    /**
     * 认领并发送,返回执行结果描述(供日志与联调接口观察)。
     */
    public String claimAndSend(Long recordId) {
        // 执行互斥:只有一个线程能把 WAITING/FAIL 翻成 SENDING
        int claimed = recordMapper.casClaimToSending(recordId, SendStatus.SENDING);
        if (claimed == 0) {
            return "SKIP: 状态不允许或已被其他线程认领";
        }

        MailSendRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            return "SKIP: 记录不存在";
        }
        try {
            return doSend(record);
        } catch (Exception e) {
            log.error("发送执行异常: recordId={}", recordId, e);
            String error = truncate("发送异常: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            recordMapper.markFail(recordId, SendStatus.FAIL, SendStatus.SENDING, error);
            return "FAIL: " + error;
        }
    }

    private String doSend(MailSendRecord record) throws Exception {
        CargoDeclareInfo config = configMapper.selectById(record.getDeclareInfoId());
        if (config == null || config.getValid() == null || config.getValid() != 1) {
            recordMapper.markFail(record.getId(), SendStatus.FAIL, SendStatus.SENDING, "群配置已删除或停用");
            return "FAIL: 群配置已删除或停用";
        }

        List<ClientEmail> emails = emailMapper.selectList(new LambdaQueryWrapper<ClientEmail>()
                .eq(ClientEmail::getDeclareInfoId, config.getId())
                .orderByAsc(ClientEmail::getId));
        List<String> to = emails.stream()
                .filter(e -> e.getEmailType() == EmailType.TO)
                .map(ClientEmail::getEmailAddress)
                .toList();
        List<String> cc = emails.stream()
                .filter(e -> e.getEmailType() == EmailType.CC)
                .map(ClientEmail::getEmailAddress)
                .toList();
        if (to.isEmpty()) {
            recordMapper.markFail(record.getId(), SendStatus.FAIL, SendStatus.SENDING, "未配置收件人(TO)");
            return "FAIL: 未配置收件人(TO)";
        }

        // 核心查询:该群截至当前尚未成功发送的消息(含迟到消息与历史 FAIL 追补)
        List<EnterpriseWechatRecord> messages =
                messageMapper.findUnsentMessages(record.getRoomId(), LocalDateTime.now());
        if (messages.isEmpty()) {
            // 决策点 D4 默认策略:无消息则不发送、不留记录(发送日志/监控可观测)
            recordMapper.deleteById(record.getId());
            log.info("群 {} 今日无待发送消息,跳过且不保留记录", record.getRoomId());
            return "SKIP: 无待发送消息,不发送";
        }

        // 重建明细(MyBatis 语句按书写顺序执行,重试场景先删后插不会撞唯一键)
        detailMapper.delete(new LambdaQueryWrapper<MailSendDetail>()
                .eq(MailSendDetail::getSendRecordId, record.getId()));
        for (EnterpriseWechatRecord msg : messages) {
            MailSendDetail detail = new MailSendDetail();
            detail.setSendRecordId(record.getId());
            detail.setMessageId(msg.getId());
            detailMapper.insert(detail);
        }
        recordMapper.updateSnapshot(record.getId(),
                String.join(";", to),
                String.join(";", cc),
                messages.size());

        String subject = "【微信群消息日报】" + config.getGroupName() + " " + record.getSendDate();
        String html = buildHtml(config, record, messages, to, cc);
        mailSender.send(to, cc, subject, html, record.getId() + "-" + record.getRoundNo());

        recordMapper.markSuccess(record.getId(), SendStatus.SUCCESS, SendStatus.SENDING);
        log.info("发送成功: recordId={} room={} 消息数={} 收件人={}",
                record.getId(), record.getRoomId(), messages.size(), to);
        return "SUCCESS: 已汇总 " + messages.size() + " 条消息发送至 [" + String.join("; ", to) + "]";
    }

    /** 固定邮件模板(不做编辑器),正文 HTML 转义防注入 */
    private String buildHtml(CargoDeclareInfo config, MailSendRecord record,
                             List<EnterpriseWechatRecord> messages, List<String> to, List<String> cc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:'Microsoft YaHei',Arial,sans-serif;max-width:720px;margin:0 auto\">");
        sb.append("<h3 style=\"border-bottom:2px solid #2563eb;padding-bottom:8px\">")
          .append(esc(config.getGroupName())).append(" ").append(record.getSendDate()).append(" 消息汇总")
          .append("<span style=\"font-size:13px;color:#64748b;font-weight:normal\">（共 ")
          .append(messages.size()).append(" 条）</span></h3>");
        sb.append("<table style=\"border-collapse:collapse;width:100%;font-size:14px\">")
          .append("<tr style=\"background:#f8fafc\">")
          .append("<th style=\"border:1px solid #e2e8f0;padding:8px;width:70px\">时间</th>")
          .append("<th style=\"border:1px solid #e2e8f0;padding:8px;width:90px\">发送人</th>")
          .append("<th style=\"border:1px solid #e2e8f0;padding:8px\">消息内容</th></tr>");
        for (EnterpriseWechatRecord m : messages) {
            sb.append("<tr>")
              .append("<td style=\"border:1px solid #e2e8f0;padding:8px\">").append(esc(m.getMsgTime().format(TIME_FMT))).append("</td>")
              .append("<td style=\"border:1px solid #e2e8f0;padding:8px\">").append(esc(m.getFromUser())).append("</td>")
              .append("<td style=\"border:1px solid #e2e8f0;padding:8px\">").append(esc(m.getMsgContent())).append("</td>")
              .append("</tr>");
        }
        sb.append("</table>");
        sb.append("<p style=\"color:#64748b;font-size:12px;margin-top:16px\">")
          .append("收件人: ").append(esc(String.join("; ", to)))
          .append(cc.isEmpty() ? "" : "｜抄送: " + esc(String.join("; ", cc)))
          .append("<br>—— 本邮件由系统自动汇总发送，请勿直接回复</p></div>");
        return sb.toString();
    }

    private String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String truncate(String s) {
        if (s == null) {
            return "unknown";
        }
        return s.length() > 1000 ? s.substring(0, 1000) : s;
    }
}
