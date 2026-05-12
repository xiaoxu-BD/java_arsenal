package org.xiaoxu.channel.retry;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xiaoxu.channel.domain.ChannelOperationLog;
import org.xiaoxu.channel.dto.ChannelRequest;
import org.xiaoxu.channel.executor.ChannelExecutor;
import org.xiaoxu.channel.mapper.ChannelLogMapper;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelRetryScheduler {

    private final ChannelLogMapper logMapper;
    private final ChannelExecutor channelExecutor;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 100;
    private static final int HARD_TIMEOUT_MINUTES = 5;

    /**
     * 每30秒扫描一次待重试流水
     * FOR UPDATE SKIP LOCKED 防止集群重复消费
     */
    @Scheduled(fixedDelay = 30000)
    public void retry() {
        List<ChannelOperationLog> pending = logMapper.selectRetryable(LocalDateTime.now(), BATCH_SIZE);
        if (pending.isEmpty()) {
            return;
        }

        log.info("【补偿扫描】捞取 {} 条待重试流水", pending.size());
        for (ChannelOperationLog channelLog : pending) {
            try {
                ChannelRequest req = rebuildRequest(channelLog);
                log.info("【补偿重试】id={}, bizId={}, retry={}/{}",
                        channelLog.getId(), channelLog.getBizId(),
                        channelLog.getRetryCount() + 1, channelLog.getMaxRetry());
                channelExecutor.doExecute(channelLog, req, Object.class);
            } catch (Exception e) {
                log.error("【补偿重试】执行异常, id={}, bizId={}", channelLog.getId(), channelLog.getBizId(), e);
            }
        }
    }

    /**
     * 每5分钟扫描PROCESSING卡死的流水，强制置TIMEOUT
     */
    @Scheduled(fixedDelay = 300000)
    public void forceTimeoutStuck() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(HARD_TIMEOUT_MINUTES);
        int count = logMapper.forceTimeoutStuck(threshold);
        if (count > 0) {
            log.warn("【硬超时】强制置超时 {} 条卡死流水", count);
        }
    }

    private ChannelRequest rebuildRequest(ChannelOperationLog log) {
        return ChannelRequest.builder()
                .channelCode(log.getChannelCode())
                .bizType(log.getBizType())
                .bizId(log.getBizId())
                .action(log.getAction())
                .url(log.getRequestUrl())
                .requestBody(log.getRequestBody())
                .maxRetry(log.getMaxRetry())
                .build();
    }
}
