-- ==================== 付款单表 ====================
CREATE TABLE IF NOT EXISTS `pay_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(64) NOT NULL COMMENT '付款单号',
    `business_type` VARCHAR(32) NOT NULL COMMENT '业务类型：leave/fulfillment',
    `business_id` BIGINT NOT NULL COMMENT '业务ID',
    `process_instance_id` VARCHAR(64) DEFAULT NULL COMMENT '流程实例ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(64) DEFAULT NULL COMMENT '用户名',
    `product_name` VARCHAR(128) NOT NULL COMMENT '商品名称',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待支付/PAID-已支付/CANCELLED-已取消/EXPIRED-已过期/REFUNDED-已退款',
    `alipay_trade_no` VARCHAR(64) DEFAULT NULL COMMENT '支付宝交易号',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `cancel_reason` VARCHAR(256) DEFAULT NULL COMMENT '取消原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_business` (`business_type`, `business_id`),
    KEY `idx_expire_time` (`expire_time`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='付款单表';

-- ==================== 支付记录表 ====================
CREATE TABLE IF NOT EXISTS `pay_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(64) NOT NULL COMMENT '付款单号',
    `alipay_trade_no` VARCHAR(64) DEFAULT NULL COMMENT '支付宝交易号',
    `trade_status` VARCHAR(32) NOT NULL COMMENT '交易状态',
    `total_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '交易金额',
    `buyer_id` VARCHAR(64) DEFAULT NULL COMMENT '买家ID',
    `raw_data` TEXT COMMENT '原始回调数据',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_alipay_trade_no` (`alipay_trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';
