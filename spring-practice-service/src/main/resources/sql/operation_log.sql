CREATE TABLE IF NOT EXISTS `operation_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `class_name`    VARCHAR(255) NOT NULL COMMENT '类名',
    `method_name`   VARCHAR(128) NOT NULL COMMENT '方法名',
    `params`        TEXT         NULL COMMENT '请求参数(JSON)',
    `result`        TEXT         NULL COMMENT '返回结果(JSON)',
    `cost_time_ms`  BIGINT       NULL COMMENT '耗时(毫秒)',
    `exception_msg` TEXT         NULL COMMENT '异常信息',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_class_method` (`class_name`, `method_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
