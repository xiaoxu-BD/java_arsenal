-- =====================================================================
-- MyBatis XML Mapper 练习专用表:商品-订单业务域
-- 表前缀统一用 practice_ ,与业务表隔离,可随时 DROP 重建
-- 关系: practice_user 1-n practice_order 1-n practice_order_item
--       practice_order_item n-1 practice_product
-- =====================================================================

DROP TABLE IF EXISTS practice_order_item;
DROP TABLE IF EXISTS practice_order;
DROP TABLE IF EXISTS practice_product;
DROP TABLE IF EXISTS practice_user;

CREATE TABLE `practice_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `phone`       VARCHAR(20)  NULL     COMMENT '手机号',
    `level`       TINYINT      NOT NULL DEFAULT 1 COMMENT '等级:1普通 2白银 3黄金',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习-用户表';

CREATE TABLE `practice_product` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_name` VARCHAR(64)   NOT NULL COMMENT '商品名',
    `category`     VARCHAR(32)   NOT NULL COMMENT '类目:DIGITAL数码/CLOTHES服饰/FOOD食品',
    `price`        DECIMAL(10,2) NOT NULL COMMENT '售价',
    `stock`        INT           NOT NULL DEFAULT 0 COMMENT '库存',
    `status`       TINYINT       NOT NULL DEFAULT 1 COMMENT '状态:0下架 1上架',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_status` (`category`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习-商品表';

CREATE TABLE `practice_order` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`     VARCHAR(32)   NOT NULL COMMENT '订单号',
    `user_id`      BIGINT        NOT NULL COMMENT '下单用户id',
    `total_amount` DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
    `order_status` TINYINT       NOT NULL COMMENT '状态:0待支付 1已支付 2已发货 3已完成 4已取消',
    `pay_time`     DATETIME      NULL     COMMENT '支付时间(未支付为NULL)',
    `remark`       VARCHAR(255)  NULL     COMMENT '备注(可能为NULL)',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_status` (`user_id`, `order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习-订单表';

CREATE TABLE `practice_order_item` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`     BIGINT        NOT NULL COMMENT '订单id',
    `product_id`   BIGINT        NOT NULL COMMENT '商品id',
    `product_name` VARCHAR(64)   NOT NULL COMMENT '商品名快照(下单时冗余,防止商品改名)',
    `quantity`     INT           NOT NULL DEFAULT 1 COMMENT '购买数量',
    `unit_price`   DECIMAL(10,2) NOT NULL COMMENT '成交单价快照',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习-订单明细表';

-- ================================ 测试数据 ================================
-- 用户:5个,等级各不相同
INSERT INTO `practice_user` (`id`, `username`, `phone`, `level`) VALUES
(1, '张三', '13800000001', 1),
(2, '李四', '13800000002', 2),
(3, '王五', '13800000003', 1),
(4, '赵六', '13800000004', 3),
(5, '钱七', '13800000005', 2);

-- 商品:10个,刻意覆盖以下练习场景:
--   不同类目/价格区间 -> if 条件过滤
--   库存为0(无线鼠标/运动鞋/黑巧克力) -> 扣库存练习
--   已下架(降噪耳机/运动鞋) -> status 过滤
INSERT INTO `practice_product` (`id`, `product_name`, `category`, `price`, `stock`, `status`) VALUES
(1, '机械键盘',   'DIGITAL',  399.00,  50,  1),
(2, '无线鼠标',   'DIGITAL',  129.00,   0,  1),
(3, '27寸显示器', 'DIGITAL', 1899.00,  10,  1),
(4, '降噪耳机',   'DIGITAL',  899.00,  30,  0),
(5, '纯棉T恤',    'CLOTHES',   99.00, 100,  1),
(6, '牛仔裤',     'CLOTHES',  299.00,  40,  1),
(7, '运动鞋',     'CLOTHES',  599.00,   0,  0),
(8, '混合坚果',   'FOOD',      49.90, 200,  1),
(9, '咖啡豆',     'FOOD',      89.00,  60,  1),
(10, '黑巧克力',  'FOOD',      35.50,   0,  1);

-- 订单:8单。每单 total_amount = 明细 sum(quantity * unit_price),数字可精确对账。
-- 刻意覆盖以下练习场景:
--   张三(1)有3单:已完成+待支付+已取消 -> 按用户统计练习
--   待支付/已取消订单 pay_time 为 NULL -> 动态SQL与IFNULL练习
--   部分订单 remark 为 NULL -> 非空判断
--   create_time 分布在 9月/10月 -> 时间范围查询
INSERT INTO `practice_order` (`id`, `order_no`, `user_id`, `total_amount`, `order_status`, `pay_time`, `remark`, `create_time`) VALUES
(1, 'SO20260901001', 1,  528.00, 3, '2026-09-01 10:05:00', '尽快发货',     '2026-09-01 09:58:00'),
(2, 'SO20260905002', 2, 1899.00, 3, '2026-09-05 14:22:00', NULL,           '2026-09-05 14:20:00'),
(3, 'SO20260918003', 1,   99.00, 4, NULL,                   '超时未支付',   '2026-09-18 20:00:00'),
(4, 'SO20260925004', 3,  227.90, 3, '2026-09-25 09:12:00', NULL,           '2026-09-25 09:10:00'),
(5, 'SO20261001005', 1,  498.00, 1, '2026-10-01 21:31:00', '国庆促销订单', '2026-10-01 21:30:00'),
(6, 'SO20261002006', 4, 1073.40, 2, '2026-10-02 11:01:00', NULL,           '2026-10-02 11:00:00'),
(7, 'SO20261002007', 5,  178.00, 0, NULL,                   NULL,           '2026-10-02 15:40:00'),
(8, 'SO20261003008', 2,  899.00, 0, NULL,                   '有人等着用',   '2026-10-03 08:15:00');

-- 订单明细:15条。一个订单1~4件商品 -> 一对多 collection 练习
-- 对账: 订单1 399+129=528 / 订单2 1899 / 订单3 99 / 订单4 89*2+49.9=227.9
--       订单5 399+99=498 / 订单6 899+89+49.9+35.5=1073.4 / 订单7 89*2=178 / 订单8 899
INSERT INTO `practice_order_item` (`id`, `order_id`, `product_id`, `product_name`, `quantity`, `unit_price`) VALUES
(1,  1, 1, '机械键盘',   1, 399.00),
(2,  1, 2, '无线鼠标',   1, 129.00),
(3,  2, 3, '27寸显示器', 1, 1899.00),
(4,  3, 5, '纯棉T恤',    1,  99.00),
(5,  4, 9, '咖啡豆',     2,  89.00),
(6,  4, 8, '混合坚果',   1,  49.90),
(7,  5, 1, '机械键盘',   1, 399.00),
(8,  5, 5, '纯棉T恤',    1,  99.00),
(9,  6, 4, '降噪耳机',   1, 899.00),
(10, 6, 9, '咖啡豆',     1,  89.00),
(11, 6, 8, '混合坚果',   1,  49.90),
(12, 6, 10, '黑巧克力',  1,  35.50),
(13, 7, 9, '咖啡豆',     2,  89.00),
(14, 8, 4, '降噪耳机',   1, 899.00);
