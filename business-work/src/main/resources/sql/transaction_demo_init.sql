-- 事务失效演示初始化脚本

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS mydb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mydb;

-- 商品表
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `modify_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品库存表
DROP TABLE IF EXISTS `t_product_stock`;
CREATE TABLE `t_product_stock` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `remain` INT NOT NULL DEFAULT 0 COMMENT '剩余库存',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `modify_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品库存表';

-- 用户表
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL COMMENT '用户名',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `modify_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 订单表
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `product_name` VARCHAR(100) COMMENT '商品名称',
  `status` TINYINT DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已取消',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `modify_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 插入测试数据
INSERT INTO `t_product` (`id`, `name`, `price`) VALUES
(1, 'iPhone 15 Pro', 8999.00),
(2, 'MacBook Pro', 14999.00),
(3, 'AirPods Pro', 1999.00);

INSERT INTO `t_product_stock` (`id`, `product_id`, `remain`) VALUES
(1, 1, 100),
(2, 2, 50),
(3, 3, 200);

INSERT INTO `t_user` (`id`, `name`, `status`) VALUES
(1, '张三', 1),
(2, '李四', 1),
(3, '王五', 0);

-- 查询验证
SELECT '商品数据' AS info;
SELECT * FROM t_product;

SELECT '库存数据' AS info;
SELECT * FROM t_product_stock;

SELECT '用户数据' AS info;
SELECT * FROM t_user;

SELECT '订单数据（应为空）' AS info;
SELECT * FROM t_order;