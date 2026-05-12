-- 渠道操作流水表
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
