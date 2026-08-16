-- ==================== sys_login_log ====================
-- 删除 browser 和 os 字段
ALTER TABLE sys_login_log DROP COLUMN browser;
ALTER TABLE sys_login_log DROP COLUMN os;

-- 添加 write_type 字段：0-同步写入，1-MQ异步写入
ALTER TABLE sys_login_log ADD COLUMN write_type TINYINT DEFAULT 1 COMMENT '写入类型：0-同步写入，1-MQ异步写入' AFTER message;

-- ==================== sys_operation_log ====================
ALTER TABLE sys_operation_log ADD COLUMN write_type TINYINT DEFAULT 1 COMMENT '写入类型：0-同步写入，1-MQ异步写入' AFTER cost_time;

-- ==================== sys_workflow_log ====================
ALTER TABLE sys_workflow_log ADD COLUMN write_type TINYINT DEFAULT 1 COMMENT '写入类型：0-同步写入，1-MQ异步写入' AFTER operator;
