-- ==================== 审计日志表 ====================

-- 1. 登录日志
CREATE TABLE IF NOT EXISTS `sys_login_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(64) COMMENT '用户名',
  `login_type` VARCHAR(20) COMMENT '登录类型: PASSWORD/EMAIL',
  `ip` VARCHAR(128) COMMENT 'IP 地址',
  `browser` VARCHAR(128) COMMENT '浏览器',
  `os` VARCHAR(128) COMMENT '操作系统',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0-成功 1-失败',
  `message` VARCHAR(255) COMMENT '提示消息',
  `login_time` DATETIME COMMENT '登录时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

-- 2. 业务操作日志
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `module` VARCHAR(64) COMMENT '操作模块',
  `operation` VARCHAR(64) COMMENT '操作类型',
  `method` VARCHAR(255) COMMENT '请求方法',
  `request_url` VARCHAR(255) COMMENT '请求 URL',
  `request_method` VARCHAR(10) COMMENT 'HTTP 方法',
  `request_params` TEXT COMMENT '请求参数',
  `response_result` TEXT COMMENT '返回结果',
  `operator` VARCHAR(64) COMMENT '操作人',
  `ip` VARCHAR(128) COMMENT 'IP 地址',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0-成功 1-失败',
  `error_msg` TEXT COMMENT '错误信息',
  `cost_time` BIGINT COMMENT '耗时(ms)',
  `create_time` DATETIME COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务操作日志';

-- 3. 工作流审批日志
CREATE TABLE IF NOT EXISTS `sys_workflow_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `process_instance_id` VARCHAR(64) COMMENT '流程实例 ID',
  `task_id` VARCHAR(64) COMMENT '任务 ID',
  `task_name` VARCHAR(128) COMMENT '任务名称',
  `business_type` VARCHAR(32) COMMENT '业务类型: leave/fulfillment',
  `business_key` VARCHAR(128) COMMENT '业务编号',
  `action` VARCHAR(20) COMMENT '操作: APPROVE/REJECT/CLAIM/SUBMIT',
  `comment` VARCHAR(500) COMMENT '审批意见',
  `operator` VARCHAR(64) COMMENT '操作人',
  `create_time` DATETIME COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流审批日志';
