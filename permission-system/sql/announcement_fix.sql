-- ================================================================
--  修复：补充公告删除权限
--  根据实际数据情况，id 使用 134 避免冲突
-- ================================================================

-- 检查并补充删除权限
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `deleted`) VALUES
(134, '公告删除', 'system:announcement:delete', 3, 4, 130, NULL, '0');

-- 给 super_admin 角色绑定删除权限（假设 role_id=1）
INSERT IGNORE INTO `system_role_menu` (`role_id`, `menu_id`, `deleted`) VALUES
(1, 134, '0');
