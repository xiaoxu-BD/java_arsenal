# 通用渠道操作流水与幂等控制方案

## 一、整体架构

```
业务方调用 ChannelExecutor.execute()
  │
  ├── ① Redis前置拦截 ──命中──→ 直接返回缓存结果
  │          │未命中
  │          ▼
  ├── ② INSERT流水（唯一索引防并发）
  │          │
  │          ▼
  ├── ③ doExecute() @Transactional
  │     ├── status=PROCESSING
  │     ├── callExternal() 调外部渠道
  │     ├── setResponseBody() 先落档
  │     ├── deserialize() 反序列化
  │     │    ├── 成功 → SUCCEED + 缓存Redis
  │     │    └── 失败 → FAILED + errorMsg
  │     └── catch 超时 → TIMEOUT + 指数退避
  │
  ├── ④ ChannelRetryScheduler 每30秒补偿扫描
  │     └── FOR UPDATE SKIP LOCKED 防集群重复消费
  │
  └── ⑤ ChannelCallbackController 渠道回调
        ├── 验签 → 解析bizId → 幂等校验 → 更新终态
        └── 防重放: status终态判断 + callback_seq_no去重
```

## 二、状态机

```
INIT → PROCESSING → SUCCEED（终态，成功）
                  → FAILED  （终态，失败，不重试）
                  → TIMEOUT → PROCESSING（补偿重试）
                               └→ retryCount >= maxRetry → FAILED
```

## 三、防并发三层屏障

```
第一层: Redis缓存拦截       → 命中直接返回，零数据库IO
第二层: MySQL唯一索引       → 同幂等键只能插入一次（DuplicateKeyException）
第三层: FOR UPDATE SKIP LOCKED → 补偿任务集群节点不重复消费
```

## 四、关键设计决策

### 4.1 先落档再反序列化

```java
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
```

**原因**：如果先 `setStatus("SUCCEED")` 再 `deserialize()`，反序列化失败会触发 `@Transactional rollback`，
导致状态回滚回 `PROCESSING` 卡死，且 response_body 也丢失。先落档保证原始报文不丢失。

### 4.2 指数退避

```java
// 10s * 2^retryCount → 10s, 20s, 40s
long delaySeconds = 10L * (1L << log.getRetryCount());
log.setNextRetryAt(LocalDateTime.now().plusSeconds(delaySeconds));
```

### 4.3 硬超时兜底

```java
// 每5分钟扫描，将超过5分钟仍处于PROCESSING的流水强制置TIMEOUT
@Scheduled(fixedDelay = 300000)
public void forceTimeoutStuck() {
    LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
    int count = logMapper.forceTimeoutStuck(threshold);
}
```

**原因**：即使事务内捕获了所有异常，仍有极端情况（如 JVM crash、线程池耗尽）导致 PROCESSING 永远不更新。
硬超时是最后的安全网。

---

## 五、完整代码

### 5.1 建表SQL

```sql
-- resources/sql/channel_operation_log.sql

CREATE TABLE `channel_operation_log` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  -- 幂等控制
  `idempotent_key`   VARCHAR(128) NOT NULL COMMENT '幂等键: channel_code:biz_type:biz_id:action',
  `channel_code`     VARCHAR(32)  NOT NULL COMMENT '渠道编码(如 ALIPAY/SF_EXPRESS)',
  `biz_type`         VARCHAR(32)  NOT NULL COMMENT '业务类型(PAYMENT/LOGISTICS/NOTIFY)',
  `biz_id`           VARCHAR(64)  NOT NULL COMMENT '业务主键ID',
  `action`           VARCHAR(32)  NOT NULL COMMENT '操作动作(CREATE/QUERY/REFUND)',
  -- 状态机
  `status`           VARCHAR(16)  NOT NULL DEFAULT 'INIT' COMMENT 'INIT/PROCESSING/SUCCEED/FAILED/TIMEOUT',
  `retry_count`      INT          NOT NULL DEFAULT 0 COMMENT '已重试次数',
  `max_retry`        INT          NOT NULL DEFAULT 3 COMMENT '最大重试次数',
  `next_retry_at`    DATETIME              COMMENT '下次重试时间(指数退避)',
  -- 报文
  `request_url`      VARCHAR(512) NOT NULL COMMENT '请求地址',
  `request_headers`  TEXT                  COMMENT '请求头(脱敏)',
  `request_body`     MEDIUMTEXT            COMMENT '请求报文',
  `response_body`    MEDIUMTEXT            COMMENT '响应报文',
  `callback_body`    MEDIUMTEXT            COMMENT '回调报文',
  `callback_seq_no`  VARCHAR(64)           COMMENT '回调序列号(防重放)',
  `error_msg`        VARCHAR(1024)         COMMENT '异常信息',
  -- 时间戳
  `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `finished_at`      DATETIME              COMMENT '终态时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_idempotent` (`idempotent_key`),
  KEY `idx_status_retry` (`status`, `next_retry_at`),
  KEY `idx_biz` (`biz_type`, `biz_id`),
  KEY `idx_callback_seq` (`channel_code`, `callback_seq_no`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道操作流水表';
```

**索引说明**：
- `uk_idempotent`：唯一索引，幂等控制核心，并发 INSERT 时只有一条成功
- `idx_status_retry`：补偿扫描查询用，定位待重试的 TIMEOUT/PROCESSING 记录
- `idx_biz`：回调按 biz_id 查流水
- `idx_callback_seq`：回调按序列号防重放

---

### 5.2 实体类 — ChannelOperationLog

```java
// channel/domain/ChannelOperationLog.java

package org.xiaoxu.channel.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("channel_operation_log")
public class ChannelOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String idempotentKey;   // 幂等键: ALIPAY:PAYMENT:ORD001:CREATE
    private String channelCode;     // 渠道编码
    private String bizType;         // 业务类型
    private String bizId;           // 业务主键
    private String action;          // 操作动作

    private String status;          // 状态机当前状态
    private Integer retryCount;     // 已重试次数
    private Integer maxRetry;       // 最大重试次数
    private LocalDateTime nextRetryAt; // 下次重试时间

    private String requestUrl;      // 请求地址
    private String requestHeaders;  // 请求头
    private String requestBody;     // 请求报文
    private String responseBody;    // 响应报文（关键：先落档再反序列化）
    private String callbackBody;    // 回调报文
    private String callbackSeqNo;   // 回调序列号
    private String errorMsg;        // 异常信息

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime finishedAt; // 终态时间

    // getter/setter 省略...
}
```

---

### 5.3 请求DTO — ChannelRequest

```java
// channel/dto/ChannelRequest.java

package org.xiaoxu.channel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRequest {

    private String channelCode;  // 渠道编码
    private String bizType;      // 业务类型
    private String bizId;        // 业务主键
    private String action;       // 操作动作

    private String url;          // 渠道地址
    private String requestBody;  // 请求体JSON

    private int maxRetry = 3;    // 最大重试次数，默认3
}
```

---

### 5.4 自定义异常 — ChannelException

```java
// channel/exception/ChannelException.java

package org.xiaoxu.channel.exception;

public class ChannelException extends RuntimeException {

    private final String channelCode;
    private final String bizId;

    public ChannelException(String message) {
        super(message);
        this.channelCode = null;
        this.bizId = null;
    }

    public ChannelException(String channelCode, String bizId, String message) {
        super(message);
        this.channelCode = channelCode;
        this.bizId = bizId;
    }

    // ⚠ 四参构造：channelCode + bizId + message + cause
    // doExecute() 中 catch 超时异常后需要传递原始 cause
    public ChannelException(String channelCode, String bizId, String message, Throwable cause) {
        super(message, cause);
        this.channelCode = channelCode;
        this.bizId = bizId;
    }

    public ChannelException(String message, Throwable cause) {
        super(message, cause);
        this.channelCode = null;
        this.bizId = null;
    }

    public String getChannelCode() { return channelCode; }
    public String getBizId() { return bizId; }
}
```

---

### 5.5 Mapper — ChannelLogMapper

```java
// channel/mapper/ChannelLogMapper.java

package org.xiaoxu.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.xiaoxu.channel.domain.ChannelOperationLog;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChannelLogMapper extends BaseMapper<ChannelOperationLog> {

    // 按幂等键查流水（并发拦截后回查已有记录）
    @Select("SELECT * FROM channel_operation_log WHERE idempotent_key = #{idempotentKey} LIMIT 1")
    ChannelOperationLog selectByIdempotentKey(@Param("idempotentKey") String idempotentKey);

    // 按渠道+业务ID查流水（回调用）
    @Select("SELECT * FROM channel_operation_log WHERE channel_code = #{channelCode} AND biz_id = #{bizId} LIMIT 1")
    ChannelOperationLog selectByBiz(@Param("channelCode") String channelCode, @Param("bizId") String bizId);

    // 按回调序列号查流水（防重放）
    @Select("SELECT * FROM channel_operation_log WHERE channel_code = #{channelCode} AND callback_seq_no = #{seqNo} LIMIT 1")
    ChannelOperationLog selectByCallbackSeqNo(@Param("channelCode") String channelCode, @Param("seqNo") String seqNo);

    // ⚠ 补偿扫描：捞取待重试流水
    // 注意：注解SQL中 < > 直接写，不需要 &lt; &gt;（那是XML mapper的写法）
    // FOR UPDATE SKIP LOCKED 防止集群多节点重复消费
    @Select({
        "<script>",
        "SELECT * FROM channel_operation_log",
        "WHERE status IN ('PROCESSING', 'TIMEOUT')",
        "  AND next_retry_at <= #{now}",
        "  AND retry_count < max_retry",
        "ORDER BY next_retry_at ASC",
        "LIMIT #{limit}",
        "FOR UPDATE SKIP LOCKED",
        "</script>"
    })
    List<ChannelOperationLog> selectRetryable(@Param("now") LocalDateTime now, @Param("limit") int limit);

    // 通用状态更新（COALESCE保证只更新非null字段，不覆盖已有数据）
    @Update({
        "UPDATE channel_operation_log SET",
        "status = #{status},",
        "retry_count = #{retryCount},",
        "next_retry_at = #{nextRetryAt},",
        "response_body = COALESCE(#{responseBody}, response_body),",
        "callback_body = COALESCE(#{callbackBody}, callback_body),",
        "callback_seq_no = COALESCE(#{callbackSeqNo}, callback_seq_no),",
        "error_msg = COALESCE(#{errorMsg}, error_msg),",
        "finished_at = COALESCE(#{finishedAt}, finished_at)",
        "WHERE id = #{id}"
    })
    int updateStatus(ChannelOperationLog log);

    // 硬超时：将超过5分钟仍PROCESSING的流水强制置TIMEOUT
    @Update({
        "UPDATE channel_operation_log SET status = 'TIMEOUT', error_msg = '硬超时: 流水挂起超过5分钟'",
        "WHERE status = 'PROCESSING'",
        "  AND created_at < #{threshold}",
        "  AND retry_count < max_retry"
    })
    int forceTimeoutStuck(@Param("threshold") LocalDateTime threshold);
}
```

---

### 5.6 核心执行器 — ChannelExecutor

```java
// channel/executor/ChannelExecutor.java

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
     * 同步执行渠道调用
     *
     * 流程：Redis拦截 → 唯一索引插入 → 渠道调用 → 状态更新
     *
     * @param req      渠道请求
     * @param respType 响应反序列化类型
     * @return 反序列化后的响应对象
     */
    public <R> R execute(ChannelRequest req, Class<R> respType) {
        String idempotentKey = buildIdempotentKey(req);

        // ① Redis前置拦截 — 命中缓存直接返回，零数据库IO
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + idempotentKey);
        if (cached != null) {
            log.info("【幂等命中Redis】key={}", idempotentKey);
            return respType.cast(cached);
        }

        // ② 原子插入 — 唯一索引防并发
        // 两个线程同时INSERT同一个idempotentKey，MySQL只允许一条成功
        ChannelOperationLog channelLog = buildLog(req, idempotentKey);
        try {
            logMapper.insert(channelLog);
        } catch (DuplicateKeyException e) {
            // 并发拦截：已有同幂等键的流水存在
            ChannelOperationLog existing = logMapper.selectByIdempotentKey(idempotentKey);
            if (existing != null && "SUCCEED".equals(existing.getStatus())) {
                // 已有流水成功了，直接返回其结果
                return deserialize(existing.getResponseBody(), respType);
            }
            // 其他线程正在处理中
            throw new ChannelException(req.getChannelCode(), req.getBizId(), "并发请求中，请稍后重试");
        }

        // ③ 渠道调用 + 状态更新（在同一事务中）
        return doExecute(channelLog, req, respType);
    }

    /**
     * 渠道调用核心逻辑（被 execute() 和 补偿扫描 共用）
     *
     * 事务边界：@Transactional 保证 PROCESSING更新、报文落档、终态更新在同一事务
     *
     * 关键设计：先 setResponseBody() 落档，再 deserialize()
     * 原因：如果先 SUCCEED 再反序列化，失败时 rollback 会丢失已收到的响应报文
     */
    @Transactional(rollbackFor = Exception.class)
    public <R> R doExecute(ChannelOperationLog log, ChannelRequest req, Class<R> respType) {
        // 标记处理中
        log.setStatus("PROCESSING");
        logMapper.updateStatus(log);

        try {
            // ④ 调用外部渠道
            String resp = callExternal(req);

            // ⑤ 先持久化响应报文（无论反序列化是否成功，原始报文已留档）
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

            // ⑦ 反序列化成功 → 终态 + 缓存Redis
            log.setStatus("SUCCEED");
            log.setFinishedAt(LocalDateTime.now());
            logMapper.updateStatus(log);

            redisTemplate.opsForValue()
                    .set(CACHE_KEY_PREFIX + log.getIdempotentKey(), result, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            return result;

        } catch (ResourceAccessException e) {
            // 连接超时 / 读取超时 → TIMEOUT（可重试，进入补偿扫描）
            handleRetryable(log, e, "渠道连接超时");
            throw new ChannelException(req.getChannelCode(), req.getBizId(), "渠道连接超时", e);
        } catch (HttpServerErrorException e) {
            // 渠道返回5xx → 按重试次数判断是否继续
            handleRetryable(log, e, "渠道服务异常: HTTP " + e.getStatusCode());
            throw new ChannelException(req.getChannelCode(), req.getBizId(), "渠道服务异常", e);
        } catch (ChannelException e) {
            // 反序列化失败已在内部处理过，直接抛出
            throw e;
        } catch (Exception e) {
            // 其他异常 → FAILED（终态）
            handleFailure(log, e);
            throw new ChannelException(req.getChannelCode(), req.getBizId(), "渠道调用失败", e);
        }
    }

    /**
     * 调用外部渠道HTTP接口
     */
    private String callExternal(ChannelRequest req) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(req.getRequestBody(), headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                req.getUrl(), HttpMethod.POST, entity, String.class);

        // 非2xx视为渠道侧异常
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new HttpServerErrorException(resp.getStatusCode());
        }
        return resp.getBody();
    }

    /**
     * 可重试异常处理 — 指数退避
     *
     * 状态转换：
     *   retryCount < maxRetry → TIMEOUT + nextRetryAt = now + 10*2^retryCount
     *   retryCount >= maxRetry → FAILED（终态，不再重试）
     */
    private void handleRetryable(ChannelOperationLog log, Exception e, String msg) {
        log.setRetryCount(log.getRetryCount() + 1);
        if (log.getRetryCount() >= log.getMaxRetry()) {
            log.setStatus("FAILED");
            log.setFinishedAt(LocalDateTime.now());
        } else {
            log.setStatus("TIMEOUT");
            // 指数退避: 10s * 2^retryCount → 10s, 20s, 40s
            long delaySeconds = 10L * (1L << log.getRetryCount());
            log.setNextRetryAt(LocalDateTime.now().plusSeconds(delaySeconds));
        }
        log.setErrorMsg(truncate(msg + ": " + e.getMessage(), 1000));
        logMapper.updateStatus(log);
    }

    /**
     * 不可重试异常处理 — 直接终态
     */
    private void handleFailure(ChannelOperationLog log, Exception e) {
        log.setStatus("FAILED");
        log.setFinishedAt(LocalDateTime.now());
        log.setErrorMsg(truncate(e.getMessage(), 1000));
        logMapper.updateStatus(log);
    }

    /**
     * 构建流水记录
     */
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

    /**
     * 幂等键规则：channelCode:bizType:bizId:action
     * 示例：ALIPAY:PAYMENT:ORD20260511001:CREATE
     */
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
```

---

### 5.7 回调控制器 — ChannelCallbackController

```java
// channel/controller/ChannelCallbackController.java

package org.xiaoxu.channel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.channel.domain.ChannelOperationLog;
import org.xiaoxu.channel.exception.ChannelException;
import org.xiaoxu.channel.mapper.ChannelLogMapper;
import org.xiaoxu.channel.sign.ChannelSignVerifier;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/callback")
@RequiredArgsConstructor
public class ChannelCallbackController {

    private final ChannelLogMapper logMapper;
    private final ChannelSignVerifier signVerifier;

    /**
     * 渠道回调入口
     *
     * 三层防护：
     * ① 验签 — 防篡改
     * ② 状态终态判断 — 已处理过的直接返回成功（防重放）
     * ③ callback_seq_no 去重 — 序列号重复的直接返回成功
     */
    @PostMapping("/{channelCode}")
    public Map<String, String> callback(@PathVariable String channelCode,
                                        @RequestHeader Map<String, String> headers,
                                        @RequestBody String body) {
        // ① 验签（防篡改）
        if (!signVerifier.verify(channelCode, headers, body)) {
            throw new ChannelException("回调签名验证失败: " + channelCode);
        }

        // ② 解析bizId（按渠道协议从body中提取）
        String bizId = extractBizId(body);
        String callbackSeqNo = headers.getOrDefault("X-Callback-Seq", null);

        // ③ 幂等校验：按 biz_id 查流水
        ChannelOperationLog channelLog = logMapper.selectByBiz(channelCode, bizId);
        if (channelLog == null) {
            return Map.of("code", "FAIL", "msg", "流水不存在");
        }

        // ④ 防重放：已终态直接返回成功
        if ("SUCCEED".equals(channelLog.getStatus()) || "FAILED".equals(channelLog.getStatus())) {
            return Map.of("code", "SUCCESS", "msg", "重复回调已忽略");
        }

        // ⑤ 序列号防重放
        if (callbackSeqNo != null) {
            ChannelOperationLog seqLog = logMapper.selectByCallbackSeqNo(channelCode, callbackSeqNo);
            if (seqLog != null) {
                return Map.of("code", "SUCCESS", "msg", "重复序列号已忽略");
            }
        }

        // ⑥ 更新流水 + 触发业务
        channelLog.setCallbackBody(body);
        channelLog.setCallbackSeqNo(callbackSeqNo);
        channelLog.setStatus("SUCCEED");
        channelLog.setFinishedAt(LocalDateTime.now());
        logMapper.updateStatus(channelLog);

        // TODO: 触发业务后续逻辑（发事件/调业务服务）
        // businessEventPublisher.publish(new ChannelCallbackEvent(bizId));

        return Map.of("code", "SUCCESS");
    }

    /**
     * 从回调报文中提取bizId — 按渠道协议实现
     * 此处为骨架，实际用JSON解析或XML解析
     */
    private String extractBizId(String body) {
        // TODO: 按渠道协议解析 body，提取 biz_id
        // 示例: return JsonPath.read(body, "$.biz_id");
        throw new ChannelException("需实现 extractBizId 方法");
    }
}
```

---

### 5.8 补偿扫描 — ChannelRetryScheduler

```java
// channel/retry/ChannelRetryScheduler.java

package org.xiaoxu.channel.retry;

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

    private static final int BATCH_SIZE = 100;
    private static final int HARD_TIMEOUT_MINUTES = 5;

    /**
     * 每30秒扫描待重试流水
     *
     * 查询条件：status=TIMEOUT 且 nextRetryAt已到 且 未超过maxRetry
     * FOR UPDATE SKIP LOCKED：集群部署时，多个节点同时扫描，被一个节点锁定的行
     *                          其他节点自动跳过，避免重复消费
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
                // 从流水记录重建请求对象
                ChannelRequest req = rebuildRequest(channelLog);
                log.info("【补偿重试】id={}, bizId={}, retry={}/{}",
                        channelLog.getId(), channelLog.getBizId(),
                        channelLog.getRetryCount() + 1, channelLog.getMaxRetry());
                // 调用 doExecute（非 execute，跳过Redis拦截和唯一索引插入）
                channelExecutor.doExecute(channelLog, req, Object.class);
            } catch (Exception e) {
                // 单条失败不影响其他流水
                log.error("【补偿重试】执行异常, id={}, bizId={}", channelLog.getId(), channelLog.getBizId(), e);
            }
        }
    }

    /**
     * 每5分钟扫描PROCESSING卡死的流水，强制置TIMEOUT
     *
     * 场景：JVM crash、线程池耗尽等极端情况导致事务未提交
     * 这些流水的status永远是PROCESSING，需要兜底处理
     */
    @Scheduled(fixedDelay = 300000)
    public void forceTimeoutStuck() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(HARD_TIMEOUT_MINUTES);
        int count = logMapper.forceTimeoutStuck(threshold);
        if (count > 0) {
            log.warn("【硬超时】强制置超时 {} 条卡死流水", count);
        }
    }

    /**
     * 从流水记录重建 ChannelRequest
     */
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
```

---

### 5.9 验签接口与默认实现

```java
// channel/sign/ChannelSignVerifier.java

package org.xiaoxu.channel.sign;

import java.util.Map;

/**
 * 渠道回调签名验证器
 *
 * 扩展方式：每个渠道实现此接口
 *   AlipaySignVerifier implements ChannelSignVerifier → RSA2验签
 *   SfSignVerifier implements ChannelSignVerifier     → HMAC-SHA256
 *   UnionPaySignVerifier implements ChannelSignVerifier → 证书验签
 */
public interface ChannelSignVerifier {

    /**
     * @param channelCode 渠道编码
     * @param headers     请求头
     * @param body        请求体
     * @return true=验签通过
     */
    boolean verify(String channelCode, Map<String, String> headers, String body);
}
```

```java
// channel/sign/DefaultChannelSignVerifier.java

package org.xiaoxu.channel.sign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class DefaultChannelSignVerifier implements ChannelSignVerifier {

    @Override
    public boolean verify(String channelCode, Map<String, String> headers, String body) {
        // TODO: 对接各渠道实际签名算法
        log.info("【验签】channelCode={}, 暂通过(待实现具体算法)", channelCode);
        return true;
    }
}
```

---

### 5.10 RestTemplate配置

```java
// channel/config/ChannelConfig.java

package org.xiaoxu.channel.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class ChannelConfig {

    /**
     * 渠道调用专用 RestTemplate
     * 连接超时3s，读取超时10s
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
```

---

## 六、调用示例

```java
// 业务代码中调用
@Autowired
private ChannelExecutor channelExecutor;

public void pay() {
    ChannelRequest req = ChannelRequest.builder()
            .channelCode("ALIPAY")
            .bizType("PAYMENT")
            .bizId("ORD20260511001")
            .action("CREATE")
            .url("https://api.alipay.com/gateway.do")
            .requestBody("{\"out_trade_no\":\"ORD20260511001\",\"total_amount\":\"100.00\"}")
            .maxRetry(3)
            .build();

    Object result = channelExecutor.execute(req, Object.class);
}
```

---

## 七、文件结构

```
business-work/src/main/java/org/xiaoxu/channel/
├── config/
│   └── ChannelConfig.java                  ← RestTemplate Bean（连接3s/读取10s超时）
├── controller/
│   └── ChannelCallbackController.java      ← POST /callback/{channelCode} 回调入口
├── domain/
│   └── ChannelOperationLog.java            ← 流水实体（MyBatis-Plus @TableName）
├── dto/
│   └── ChannelRequest.java                 ← 渠道请求DTO（Builder模式）
├── exception/
│   └── ChannelException.java               ← 渠道异常（含channelCode/bizId上下文）
├── executor/
│   └── ChannelExecutor.java                ← 核心执行器（幂等+防并发+事务+退避重试）
├── mapper/
│   └── ChannelLogMapper.java               ← 自定义SQL（selectRetryable/forceTimeoutStuck）
├── retry/
│   └── ChannelRetryScheduler.java          ← 定时补偿（30s重试 + 5min硬超时扫描）
└── sign/
    ├── ChannelSignVerifier.java            ← 验签接口
    └── DefaultChannelSignVerifier.java     ← 默认实现（待扩展具体渠道算法）

business-work/src/main/resources/
└── sql/
    └── channel_operation_log.sql           ← DDL建表语句
```
