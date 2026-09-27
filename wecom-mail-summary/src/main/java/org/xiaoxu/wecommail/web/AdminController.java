package org.xiaoxu.wecommail.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.wecommail.common.ApiResponse;
import org.xiaoxu.wecommail.common.BusinessException;
import org.xiaoxu.wecommail.common.EmailType;
import org.xiaoxu.wecommail.common.SendStatus;
import org.xiaoxu.wecommail.entity.CargoDeclareInfo;
import org.xiaoxu.wecommail.entity.ClientEmail;
import org.xiaoxu.wecommail.entity.EnterpriseWechatRecord;
import org.xiaoxu.wecommail.entity.MailSendDetail;
import org.xiaoxu.wecommail.entity.MailSendRecord;
import org.xiaoxu.wecommail.mail.MailSendService;
import org.xiaoxu.wecommail.mapper.CargoDeclareInfoMapper;
import org.xiaoxu.wecommail.mapper.ClientEmailMapper;
import org.xiaoxu.wecommail.mapper.EnterpriseWechatRecordMapper;
import org.xiaoxu.wecommail.mapper.MailSendDetailMapper;
import org.xiaoxu.wecommail.mapper.MailSendRecordMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 管理后台 REST API,与管理页面交互一一对应(设计文档 §11)。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminController {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final CargoDeclareInfoMapper configMapper;
    private final ClientEmailMapper emailMapper;
    private final EnterpriseWechatRecordMapper messageMapper;
    private final MailSendRecordMapper recordMapper;
    private final MailSendDetailMapper detailMapper;
    private final MailSendService mailSendService;
    private final WechatRoomClient roomClient;

    public record SaveGroupReq(String roomId, String groupName) {
    }

    public record SaveEmailsReq(String sendTime, List<String> to, List<String> cc) {
    }

    // ==================== 群配置 ====================

    /** 主表格:群配置列表(含邮箱、发送时间、今日消息数) */
    @GetMapping("/declare-infos")
    public ApiResponse<List<Map<String, Object>>> listGroups() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();
        List<CargoDeclareInfo> configs = configMapper.selectList(
                new LambdaQueryWrapper<CargoDeclareInfo>()
                        .eq(CargoDeclareInfo::getValid, 1)
                        .orderByAsc(CargoDeclareInfo::getId));
        for (CargoDeclareInfo c : configs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("roomId", c.getRoomId());
            item.put("groupName", c.getGroupName());
            item.put("sendTime", c.getSendTime().toString());
            item.put("todayMessageCount", messageMapper.selectCount(new LambdaQueryWrapper<EnterpriseWechatRecord>()
                    .eq(EnterpriseWechatRecord::getRoomId, c.getRoomId())
                    .between(EnterpriseWechatRecord::getMsgTime, today.atStartOfDay(), today.plusDays(1).atStartOfDay())));
            item.put("emails", splitEmails(c.getId()));
            result.add(item);
        }
        return ApiResponse.ok(result);
    }

    /** 「新增群」下拉数据源:后端提供(url 未配置=后端写死 Mock,已配置=调其他组接口),仅 roomId,落库缓存,附 configured 标记 */
    @GetMapping("/wechat-rooms")
    public ApiResponse<List<Map<String, Object>>> listRooms() {
        String source = roomClient.isMockMode() ? "MOCK" : "HTTP";
        List<Map<String, Object>> result = roomClient.listAllRooms(false).stream()
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<String, Object>();
                    m.put("roomId", r.getRoomId());
                    m.put("configured", configMapper.selectCount(new LambdaQueryWrapper<CargoDeclareInfo>()
                            .eq(CargoDeclareInfo::getRoomId, r.getRoomId())
                            .eq(CargoDeclareInfo::getValid, 1)) > 0);
                    m.put("source", source);
                    return m;
                })
                .toList();
        return ApiResponse.ok(result);
    }

    /**
     * 新增群:roomId 必须来自群列表;群名称必填——群列表接口无法提供群名,由管理员人工确认。
     * 后端二次校验去重;复活式处理逻辑删除记录。
     */
    @PostMapping("/declare-infos")
    @Transactional
    public ApiResponse<Map<String, Object>> createGroup(@RequestBody SaveGroupReq req) {
        String roomId = req.roomId() == null ? "" : req.roomId().trim();
        String groupName = req.groupName() == null ? "" : req.groupName().trim();
        if (roomId.isEmpty()) {
            throw new BusinessException("roomId 不能为空");
        }
        if (groupName.isEmpty()) {
            throw new BusinessException("群名称不能为空(群列表接口只提供 roomId,群名称需人工确认填写)");
        }
        boolean roomKnown = roomClient.listAllRooms(false).stream()
                .anyMatch(r -> r.getRoomId().equals(roomId));
        if (!roomKnown) {
            throw new BusinessException("roomId 不在群列表中: " + roomId);
        }
        // 保存前二次校验 roomId 是否已配置(与前端校验形成双保险)
        CargoDeclareInfo saved;
        CargoDeclareInfo existing = configMapper.selectOne(new LambdaQueryWrapper<CargoDeclareInfo>()
                .eq(CargoDeclareInfo::getRoomId, roomId));
        if (existing != null) {
            if (existing.getValid() != null && existing.getValid() == 1) {
                throw new BusinessException("roomId 已配置,不可重复: " + roomId);
            }
            // 复活式更新:绕开 uk_room_id 与逻辑删除的冲突
            existing.setValid(1);
            existing.setGroupName(groupName);
            if (existing.getSendTime() == null) {
                existing.setSendTime(LocalTime.of(18, 0));
            }
            configMapper.updateById(existing);
            saved = existing;
        } else {
            CargoDeclareInfo entity = new CargoDeclareInfo();
            entity.setRoomId(roomId);
            entity.setGroupName(groupName);
            entity.setSendTime(LocalTime.of(18, 0));
            entity.setValid(1);
            try {
                configMapper.insert(entity);
                saved = entity;
            } catch (Exception e) {
                throw new BusinessException("roomId 已配置(并发写入),不可重复: " + roomId);
            }
        }
        return ApiResponse.ok(Map.of("id", saved.getId(), "roomId", saved.getRoomId(),
                "groupName", saved.getGroupName(), "sendTime", saved.getSendTime().toString()));
    }

    /** 修改群:roomId 不允许修改(后端直接忽略该字段),仅可改群名称 */
    @PutMapping("/declare-infos/{id}")
    public ApiResponse<Map<String, Object>> updateGroup(@PathVariable Long id,
                                                        @RequestBody SaveGroupReq req) {
        CargoDeclareInfo config = requireValidConfig(id);
        String groupName = req.groupName() == null ? "" : req.groupName().trim();
        if (groupName.isEmpty()) {
            throw new BusinessException("群名称不能为空");
        }
        config.setGroupName(groupName);
        configMapper.updateById(config);
        return ApiResponse.ok(Map.of("id", config.getId(), "roomId", config.getRoomId(),
                "groupName", config.getGroupName()));
    }

    /** 删除群:逻辑删除,历史发送记录保留可查 */
    @DeleteMapping("/declare-infos/{id}")
    @Transactional
    public ApiResponse<Void> deleteGroup(@PathVariable Long id) {
        CargoDeclareInfo config = requireValidConfig(id);
        config.setValid(0);
        configMapper.updateById(config);
        emailMapper.delete(new LambdaQueryWrapper<ClientEmail>().eq(ClientEmail::getDeclareInfoId, id));
        return ApiResponse.ok();
    }

    /** 配置邮件:TO/CC + 每日发送时间,以群为单位整体覆盖;后端做格式校验与去重 */
    @PutMapping("/declare-infos/{id}/emails")
    @Transactional
    public ApiResponse<Map<String, Object>> saveEmails(@PathVariable Long id,
                                                       @RequestBody SaveEmailsReq req) {
        CargoDeclareInfo config = requireValidConfig(id);
        List<String> to = req.to() == null ? List.of() : req.to();
        List<String> cc = req.cc() == null ? List.of() : req.cc();
        if (to.isEmpty()) {
            throw new BusinessException("请至少配置一个收件人(TO)");
        }
        for (String email : to) {
            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                throw new BusinessException("收件人邮箱格式不正确: " + email);
            }
        }
        for (String email : cc) {
            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                throw new BusinessException("抄送人邮箱格式不正确: " + email);
            }
        }
        LocalTime sendTime;
        try {
            sendTime = LocalTime.parse(req.sendTime() == null ? "" : req.sendTime());
        } catch (DateTimeParseException e) {
            throw new BusinessException("发送时间格式不正确,应为 HH:mm: " + req.sendTime());
        }

        emailMapper.delete(new LambdaQueryWrapper<ClientEmail>().eq(ClientEmail::getDeclareInfoId, id));
        for (String email : to) {
            saveEmail(id, email.trim(), EmailType.TO);
        }
        for (String email : cc) {
            saveEmail(id, email.trim(), EmailType.CC);
        }
        config.setSendTime(sendTime);
        configMapper.updateById(config);
        return ApiResponse.ok(Map.of("id", id, "sendTime", sendTime.toString(),
                "to", to, "cc", cc));
    }

    /** 今日消息:发送人/消息时间/消息内容 */
    @GetMapping("/declare-infos/{id}/messages/today")
    public ApiResponse<List<Map<String, Object>>> todayMessages(@PathVariable Long id) {
        CargoDeclareInfo config = requireValidConfig(id);
        LocalDate today = LocalDate.now();
        List<EnterpriseWechatRecord> list = messageMapper.selectList(
                new LambdaQueryWrapper<EnterpriseWechatRecord>()
                        .eq(EnterpriseWechatRecord::getRoomId, config.getRoomId())
                        .between(EnterpriseWechatRecord::getMsgTime, today.atStartOfDay(), today.plusDays(1).atStartOfDay())
                        .orderByAsc(EnterpriseWechatRecord::getMsgTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (EnterpriseWechatRecord m : list) {
            result.add(messageView(m));
        }
        return ApiResponse.ok(result);
    }

    // ==================== 发送记录 ====================

    /** 发送记录列表(弹窗) */
    @GetMapping("/send-records")
    public ApiResponse<List<Map<String, Object>>> listRecords(@RequestParam(defaultValue = "20") int limit) {
        List<MailSendRecord> records = recordMapper.findLatest(Math.min(Math.max(limit, 1), 100));
        List<Map<String, Object>> result = new ArrayList<>();
        for (MailSendRecord r : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getId());
            item.put("sendDate", r.getSendDate().toString());
            item.put("roundNo", r.getRoundNo());
            item.put("sendTime", r.getSendTime() == null ? null : r.getSendTime().toString());
            item.put("createTime", r.getCreateTime().toString());
            item.put("roomId", r.getRoomId());
            item.put("groupName", groupNameOf(r));
            item.put("sendStatus", r.getSendStatus().name());
            item.put("messageCount", r.getMessageCount());
            item.put("toAddresses", r.getToAddresses());
            item.put("ccAddresses", r.getCcAddresses() == null ? "" : r.getCcAddresses());
            item.put("errorMessage", r.getErrorMessage() == null ? "" : r.getErrorMessage());
            item.put("retryCount", r.getRetryCount());
            result.add(item);
        }
        return ApiResponse.ok(result);
    }

    /** 发送详情(弹窗):记录字段 + 本封邮件包含的微信群消息 */
    @GetMapping("/send-records/{id}")
    public ApiResponse<Map<String, Object>> recordDetail(@PathVariable Long id) {
        MailSendRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("发送记录不存在: id=" + id);
        }
        List<Long> messageIds = detailMapper.selectList(new LambdaQueryWrapper<MailSendDetail>()
                        .eq(MailSendDetail::getSendRecordId, id)
                        .orderByAsc(MailSendDetail::getId))
                .stream().map(MailSendDetail::getMessageId).toList();
        List<Map<String, Object>> messages = messageIds.isEmpty() ? List.of()
                : messageMapper.selectBatchIds(messageIds).stream()
                        .sorted(Comparator.comparing(EnterpriseWechatRecord::getMsgTime))
                        .map(this::messageView)
                        .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", record.getId());
        result.put("sendDate", record.getSendDate().toString());
        result.put("roundNo", record.getRoundNo());
        result.put("sendTime", record.getSendTime() == null ? null : record.getSendTime().toString());
        result.put("roomId", record.getRoomId());
        result.put("groupName", groupNameOf(record));
        result.put("sendStatus", record.getSendStatus().name());
        result.put("toAddresses", record.getToAddresses());
        result.put("ccAddresses", record.getCcAddresses() == null ? "" : record.getCcAddresses());
        result.put("errorMessage", record.getErrorMessage() == null ? "" : record.getErrorMessage());
        result.put("retryCount", record.getRetryCount());
        result.put("messageCount", record.getMessageCount());
        result.put("messages", messages);
        return ApiResponse.ok(result);
    }

    /** 手动重发:仅 FAIL 状态允许(预留接口,页面暂无按钮);直接重新走 CAS 认领 */
    @PostMapping("/send-records/{id}/resend")
    public ApiResponse<String> resend(@PathVariable Long id) {
        MailSendRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("发送记录不存在: id=" + id);
        }
        if (record.getSendStatus() != SendStatus.FAIL) {
            throw new BusinessException("仅 FAIL 状态的记录允许手动重发,当前: " + record.getSendStatus());
        }
        String result = mailSendService.claimAndSend(id);
        return ApiResponse.ok(result);
    }

    // ==================== 私有工具 ====================

    private CargoDeclareInfo requireValidConfig(Long id) {
        CargoDeclareInfo config = configMapper.selectById(id);
        if (config == null || config.getValid() == null || config.getValid() != 1) {
            throw new BusinessException("群配置不存在: id=" + id);
        }
        return config;
    }

    private void saveEmail(Long declareInfoId, String address, EmailType type) {
        ClientEmail email = new ClientEmail();
        email.setDeclareInfoId(declareInfoId);
        email.setEmailAddress(address);
        email.setEmailType(type);
        emailMapper.insert(email);
    }

    private Map<String, EmailType> splitEmails(Long declareInfoId) {
        Map<String, EmailType> map = new LinkedHashMap<>();
        for (ClientEmail e : emailMapper.selectList(new LambdaQueryWrapper<ClientEmail>()
                .eq(ClientEmail::getDeclareInfoId, declareInfoId)
                .orderByAsc(ClientEmail::getId))) {
            map.put(e.getEmailAddress(), e.getEmailType());
        }
        return map;
    }

    private String groupNameOf(MailSendRecord record) {
        CargoDeclareInfo config = configMapper.selectById(record.getDeclareInfoId());
        if (config == null) {
            return record.getRoomId() + "(配置已删除)";
        }
        return config.getGroupName() + (config.getValid() != null && config.getValid() == 1 ? "" : "(已删除)");
    }

    private Map<String, Object> messageView(EnterpriseWechatRecord m) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("msgId", m.getMsgId());
        view.put("fromUser", m.getFromUser());
        view.put("msgTime", m.getMsgTime().toString());
        view.put("content", m.getMsgContent());
        return view;
    }
}
