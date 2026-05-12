package org.xiaoxu.channel.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.xiaoxu.channel.domain.ChannelOperationLog;
import org.xiaoxu.channel.dto.ChannelRequest;
import org.xiaoxu.channel.exception.ChannelException;
import org.xiaoxu.channel.mapper.ChannelLogMapper;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelExecutor {

    private final ChannelLogMapper logMapper;
    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "channel:idempotent:";
    private static final long CACHE_TTL_MINUTES = 5;

    /**
     * 同步执行渠道调用（幂等 + 防并发 + 事务绑定）
     *
     * @param req      渠道请求
     * @param respType 响应类型
     * @return 反序列化的响应对象
     */
    public <R> R execute(ChannelRequest req, Class<R> respType) {
        String idempotentKey = buildIdempotentKey(req);

        // ① Redis前置拦截
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + idempotentKey);
        if (cached != null) {
            log.info("【幂等命中Redis】key={}", idempotentKey);
            //不懂什么意思？
            return respType.cast(cached);
        }

        // ② 原子插入（唯一索引防并发）
        ChannelOperationLog log = buildLog(req, idempotentKey);
        try {
            logMapper.insert(log);
        } catch (DuplicateKeyException e) {
//            log.info("【并发拦截】唯一索引冲突, idempotentKey={}", idempotentKey);
            ChannelOperationLog existing = logMapper.selectByIdempotentKey(idempotentKey);
            if (existing != null && "SUCCEED".equals(existing.getStatus())) {
                return deserialize(existing.getResponseBody(), respType);
            }
            throw new ChannelException(req.getChannelCode(), req.getBizId(), "并发请求中，请稍后重试");
        }

        // ③ 渠道调用 + 状态更新（同一事务）
        return doExecute(log, req, respType);
    }


    //渠道调用和状态更新是放在同一个事务中；
    @Transactional(rollbackFor = Exception.class)
    public <R> R doExecute(ChannelOperationLog log, ChannelRequest req, Class<R> respType) {
        log.setStatus("PROCESSING");
        logMapper.updateStatus(log);

        try {
            // ④ 调用外部渠道
            String resp = callExternal(req);

            // ⑤ 先持久化响应报文（无论反序列化是否成功，报文已留档）
            log.setResponseBody(resp);

            // ⑥ 反序列化（渠道返回非JSON会在这里失败）
            R result;
            try {
                result = deserialize(resp, respType);
            } catch (ChannelException e) {
                // 反序列化失败：响应已存档，状态置FAILED，不重试（非网络问题）
                log.setStatus("FAILED");
                log.setFinishedAt(LocalDateTime.now());
                log.setErrorMsg("响应反序列化失败(非JSON): " + truncate(resp, 500));
                logMapper.updateStatus(log);
                throw e;
            }

            // ⑦ 反序列化成功 → 终态 + 缓存
            log.setStatus("SUCCEED");
            log.setFinishedAt(LocalDateTime.now());
            logMapper.updateStatus(log);

            redisTemplate.opsForValue()
                    .set(CACHE_KEY_PREFIX + log.getIdempotentKey(), result, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            return result;

        } catch (ResourceAccessException e) {
            // 连接超时 / 读取超时 → TIMEOUT（可重试）
            handleRetryable(log, e, "渠道连接超时");
            throw new ChannelException(req.getChannelCode(), req.getBizId(), "渠道连接超时", e);
        } catch (HttpServerErrorException e) {
            // 渠道返回5xx → FAILED（按业务判断是否重试）
            handleRetryable(log, e, "渠道服务异常: HTTP " + e.getStatusCode());
            throw new ChannelException(req.getChannelCode(), req.getBizId(), "渠道服务异常", e);
        } catch (ChannelException e) {
            // 反序列化失败已处理过，直接抛出
            throw e;
        } catch (Exception e) {
            // 其他异常 → FAILED
            handleFailure(log, e);
            throw new ChannelException(req.getChannelCode(), req.getBizId(), "渠道调用失败", e);
        }
    }

    private String callExternal(ChannelRequest req) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(req.getRequestBody(), headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                req.getUrl(), HttpMethod.POST, entity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new HttpServerErrorException(resp.getStatusCode());
        }
        return resp.getBody();
    }

    private void handleRetryable(ChannelOperationLog log, Exception e, String msg) {
        log.setRetryCount(log.getRetryCount() + 1);
        if (log.getRetryCount() >= log.getMaxRetry()) {
            log.setStatus("FAILED");
            log.setFinishedAt(LocalDateTime.now());
        } else {
            log.setStatus("TIMEOUT");
            // 指数退避: 10s * 2^retryCount (10s, 20s, 40s...)
            long delaySeconds = 10L * (1L << log.getRetryCount());
            log.setNextRetryAt(LocalDateTime.now().plusSeconds(delaySeconds));
        }
        log.setErrorMsg(truncate(msg + ": " + e.getMessage(), 1000));
        logMapper.updateStatus(log);
    }

    private void handleFailure(ChannelOperationLog log, Exception e) {
        log.setStatus("FAILED");
        log.setFinishedAt(LocalDateTime.now());
        log.setErrorMsg(truncate(e.getMessage(), 1000));
        logMapper.updateStatus(log);
    }

    private ChannelOperationLog buildLog(ChannelRequest req, String idempotentKey) {
        ChannelOperationLog log = new ChannelOperationLog();
        log.setIdempotentKey(idempotentKey);
        log.setChannelCode(req.getChannelCode());
        log.setBizType(req.getBizType());
        log.setBizId(req.getBizId());
        log.setAction(req.getAction());
        log.setStatus("INIT");
        log.setRetryCount(0);
        log.setMaxRetry(req.getMaxRetry());
        log.setRequestUrl(req.getUrl());
        log.setRequestBody(req.getRequestBody());
        return log;
    }

    //ALIPAY:PAYMENT:ORD20260511001：CREATE
    private String buildIdempotentKey(ChannelRequest req) {
        return req.getChannelCode() + ":" + req.getBizType() + ":" + req.getBizId() + ":" + req.getAction();
    }

    private <R> R deserialize(String json, Class<R> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new ChannelException("响应反序列化失败", e);
        }
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() > maxLen ? str.substring(0, maxLen) : str;
    }
}
