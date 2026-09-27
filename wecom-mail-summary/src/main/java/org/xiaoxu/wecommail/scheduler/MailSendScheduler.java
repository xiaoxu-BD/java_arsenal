package org.xiaoxu.wecommail.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xiaoxu.wecommail.common.SendStatus;
import org.xiaoxu.wecommail.config.WecomMailProperties;
import org.xiaoxu.wecommail.entity.CargoDeclareInfo;
import org.xiaoxu.wecommail.entity.MailSendRecord;
import org.xiaoxu.wecommail.mail.MailSendService;
import org.xiaoxu.wecommail.mapper.CargoDeclareInfoMapper;
import org.xiaoxu.wecommail.mapper.EnterpriseWechatRecordMapper;
import org.xiaoxu.wecommail.mapper.MailSendRecordMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 每分钟扫描(单实例单线程执行;多实例由 uk_group_date + CAS 保证互斥,见设计文档 §10):
 *
 * <p>1) SENDING 超时兜底:卡死记录强制置 FAIL;
 * <p>2) 到点建任务:valid=1 且 send_time 已到、今日无记录的群创建 WAITING(send_time<=now 天然支持停机补发);
 * <p>3) 认领执行:WAITING 或退避到期的 FAIL 提交线程池执行。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailSendScheduler {

    private final CargoDeclareInfoMapper configMapper;
    private final MailSendRecordMapper recordMapper;
    private final EnterpriseWechatRecordMapper messageMapper;
    private final MailSendService mailSendService;
    private final WecomMailProperties props;

    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    /** 记录级在途标记,避免异步执行期间被下一次扫描重复提交(最终仍由 CAS 兜底) */
    private final Set<Long> inFlight = ConcurrentHashMap.newKeySet();

    @Scheduled(cron = "${wecom.mail.send.schedule-cron:0 * * * * *}")
    public void scheduledScan() {
        try {
            scan();
        } catch (Exception e) {
            log.error("定时扫描异常", e);
        }
    }

    public synchronized void scan() {
        WecomMailProperties.Mail.Send sendCfg = props.getMail().getSend();
        LocalDate today = LocalDate.now();

        // 1) 卡死兜底:SENDING 超过阈值(实例宕机)→ FAIL,允许重试
        int stuck = recordMapper.markStuckSendingFailed(
                SendStatus.SENDING, SendStatus.FAIL,
                "发送超时: SENDING 超过 " + sendCfg.getSendingTimeoutMinutes() + " 分钟,疑似实例宕机",
                LocalDateTime.now().minusMinutes(sendCfg.getSendingTimeoutMinutes()));
        if (stuck > 0) {
            log.warn("本次扫描将 {} 条卡死的 SENDING 记录置为 FAIL", stuck);
        }

        // 2) 逐群判断:到点且有待发消息 → 无轮次建第1轮;最新轮 SUCCESS 且过了补发延迟且又有新消息 → 建补充轮
        //    (FAIL/WAITING 不在此处建新轮:FAIL 走第3步同轮退避重试,WAITING 走第3步认领)
        LocalTime now = LocalTime.now();
        List<CargoDeclareInfo> configs = configMapper.selectList(new LambdaQueryWrapper<CargoDeclareInfo>()
                .eq(CargoDeclareInfo::getValid, 1)
                .orderByAsc(CargoDeclareInfo::getSendTime)
                .orderByAsc(CargoDeclareInfo::getId));
        for (CargoDeclareInfo config : configs) {
            if (config.getSendTime() == null || config.getSendTime().isAfter(now)) {
                continue;
            }
            List<MailSendRecord> latestList = recordMapper.selectList(new LambdaQueryWrapper<MailSendRecord>()
                    .eq(MailSendRecord::getDeclareInfoId, config.getId())
                    .eq(MailSendRecord::getSendDate, today)
                    .orderByDesc(MailSendRecord::getRoundNo)
                    .last("LIMIT 1"));
            MailSendRecord latest = latestList.isEmpty() ? null : latestList.get(0);
            if (latest == null) {
                createRound(config, today, 1);
            } else if (latest.getSendStatus() == SendStatus.SUCCESS && latest.getSendTime() != null) {
                boolean cooldownPassed = LocalDateTime.now()
                        .isAfter(latest.getSendTime().plusMinutes(sendCfg.getLateResendDelayMinutes()));
                if (cooldownPassed && hasUnsentMessages(config.getRoomId())) {
                    log.info("群 {} 当日已有 SUCCESS 第{}轮,检测到迟到未发送消息,创建补充轮",
                            config.getRoomId(), latest.getRoundNo());
                    createRound(config, today, latest.getRoundNo() + 1);
                }
            }
        }

        // 3) 认领执行:WAITING 直接执行;FAIL 判断退避到期后重试(同轮)
        List<MailSendRecord> todayRecords = recordMapper.selectList(
                new LambdaQueryWrapper<MailSendRecord>().eq(MailSendRecord::getSendDate, today));
        for (MailSendRecord record : todayRecords) {
            boolean shouldDispatch = false;
            if (record.getSendStatus() == SendStatus.WAITING) {
                shouldDispatch = true;
            } else if (record.getSendStatus() == SendStatus.FAIL) {
                shouldDispatch = retryDue(record);
            }
            if (shouldDispatch && inFlight.add(record.getId())) {
                pool.submit(() -> {
                    try {
                        String result = mailSendService.claimAndSend(record.getId());
                        log.info("发送任务 #{} 执行结果: {}", record.getId(), result);
                    } catch (Exception e) {
                        log.error("发送任务 #{} 执行异常", record.getId(), e);
                    } finally {
                        inFlight.remove(record.getId());
                    }
                });
            }
        }
    }

    /** 建一轮发送任务;无待发消息则不落记录(决策点 D4) */
    private void createRound(CargoDeclareInfo config, LocalDate today, int roundNo) {
        if (!hasUnsentMessages(config.getRoomId())) {
            return;
        }
        MailSendRecord waiting = new MailSendRecord();
        waiting.setDeclareInfoId(config.getId());
        waiting.setRoomId(config.getRoomId());
        waiting.setSendDate(today);
        waiting.setRoundNo(roundNo);
        waiting.setSendStatus(SendStatus.WAITING);
        waiting.setRetryCount(0);
        waiting.setMessageCount(0);
        try {
            recordMapper.insert(waiting);
            log.info("创建发送任务: room={} group={} date={} round={} time={}",
                    config.getRoomId(), config.getGroupName(), today, roundNo, config.getSendTime());
        } catch (DataIntegrityViolationException e) {
            // uk(群,日,轮次) 冲突:多实例并发下已被其他实例创建,忽略
        }
    }

    private boolean hasUnsentMessages(String roomId) {
        return !messageMapper.findUnsentMessages(roomId, LocalDateTime.now()).isEmpty();
    }

    /** 重试条件:次数未超限 && 上次尝试后已退避对应间隔 */
    private boolean retryDue(MailSendRecord record) {
        WecomMailProperties.Mail.Send sendCfg = props.getMail().getSend();
        if (record.getRetryCount() >= sendCfg.getRetryMaxCount()) {
            return false;
        }
        List<Integer> backoff = sendCfg.getRetryBackoffMinutes();
        int idx = Math.min(Math.max(record.getRetryCount(), 1), backoff.size()) - 1;
        LocalDateTime nextRetryTime = record.getUpdateTime().plusMinutes(backoff.get(idx));
        return LocalDateTime.now().isAfter(nextRetryTime);
    }

    @PreDestroy
    public void shutdown() {
        pool.shutdown();
    }
}
