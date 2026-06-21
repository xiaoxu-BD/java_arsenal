-- ================================================================
--  为 system_users 表添加 manager_id 字段，支持"发起人上级"审批人策略
-- ================================================================

ALTER TABLE `system_users`
ADD COLUMN `manager_id` BIGINT DEFAULT NULL COMMENT '直属上级用户ID' AFTER `dept_id`;

-- 添加外键注释（逻辑外键，不建物理外键以保持灵活性）
-- manager_id 引用 system_users.id

-- 示例：设置用户的上级关系
-- UPDATE `system_users` SET `manager_id` = 1 WHERE `username` = 'manager';
-- UPDATE `system_users` SET `manager_id` = (SELECT id FROM (SELECT id FROM system_users WHERE username = 'admin') AS tmp) WHERE `username` = 'manager';
