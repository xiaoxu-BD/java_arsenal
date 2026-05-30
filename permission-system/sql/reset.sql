-- ================================================================
--  清空所有表数据并重置 AUTO_INCREMENT 从 1 开始
--  执行顺序：先删关联表，再删主表（避免外键约束问题）
-- ================================================================

USE `2026mysql_ds`;

-- 1. 清空关联表
TRUNCATE TABLE `system_user_role`;
TRUNCATE TABLE `system_role_menu`;

-- 2. 清空主表
TRUNCATE TABLE `system_menu`;
TRUNCATE TABLE `system_role`;
TRUNCATE TABLE `system_users`;

-- TRUNCATE 会自动重置 AUTO_INCREMENT 为 1
-- 验证：
-- SHOW CREATE TABLE system_users;
-- SHOW CREATE TABLE system_role;
-- SHOW CREATE TABLE system_menu;

-- ================================================================
--  重新插入初始数据（ID 从 1 开始）
-- ================================================================

-- 用户
INSERT INTO `system_users` (`id`, `username`, `password`, `nickname`, `status`, `deleted`)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 0, '0');

-- 角色
INSERT INTO `system_role` (`id`, `name`, `code`, `sort`, `data_scope`, `deleted`) VALUES
(1, '超级管理员', 'super_admin', 1, 1, '0'),
(2, '普通用户',   'user',        2, 5, '0');

-- 菜单 - 顶级目录
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `visible`, `deleted`) VALUES
(1, '系统管理', NULL, 1, 1, 0, '/system', 'icon-settings', NULL, '0', '0'),
(2, '首页',     NULL, 2, 0, 0, '/',       'icon-home',     '@/views/dashboard/index.vue', '0', '0');

-- 菜单 - 子菜单
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `visible`, `deleted`) VALUES
(3, '用户管理', NULL, 2, 1, 1, 'user', 'icon-user',       '@/views/user/index.vue', '0', '0'),
(4, '角色管理', NULL, 2, 2, 1, 'role', 'icon-user-group', '@/views/role/index.vue', '0', '0'),
(5, '菜单管理', NULL, 2, 3, 1, 'menu', 'icon-menu',       '@/views/menu/index.vue', '0', '0');

-- 按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `deleted`) VALUES
(6,  '用户查询', 'system:user:query',  3, 1, 3, NULL, '0'),
(7,  '用户新增', 'system:user:create', 3, 2, 3, NULL, '0'),
(8,  '用户修改', 'system:user:update', 3, 3, 3, NULL, '0'),
(9,  '用户删除', 'system:user:delete', 3, 4, 3, NULL, '0'),
(10, '角色查询', 'system:role:query',  3, 1, 4, NULL, '0'),
(11, '角色新增', 'system:role:create', 3, 2, 4, NULL, '0'),
(12, '角色修改', 'system:role:update', 3, 3, 4, NULL, '0'),
(13, '角色删除', 'system:role:delete', 3, 4, 4, NULL, '0'),
(14, '菜单查询', 'system:menu:query',  3, 1, 5, NULL, '0'),
(15, '菜单新增', 'system:menu:create', 3, 2, 5, NULL, '0'),
(16, '菜单修改', 'system:menu:update', 3, 3, 5, NULL, '0'),
(17, '菜单删除', 'system:menu:delete', 3, 4, 5, NULL, '0');

-- 用户-角色关联 (admin → super_admin)
INSERT INTO `system_user_role` (`user_id`, `role_id`, `deleted`) VALUES (1, 1, '0');

-- 角色-菜单关联 (super_admin → 所有菜单)
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `deleted`) VALUES
(1, 1, '0'), (1, 2, '0'), (1, 3, '0'), (1, 4, '0'), (1, 5, '0'),
(1, 6, '0'), (1, 7, '0'), (1, 8, '0'), (1, 9, '0'),
(1,10, '0'), (1,11, '0'), (1,12, '0'), (1,13, '0'),
(1,14, '0'), (1,15, '0'), (1,16, '0'), (1,17, '0');

-- 验证
SELECT 'system_users' AS tbl, COUNT(*) AS cnt FROM system_users
UNION ALL SELECT 'system_role', COUNT(*) FROM system_role
UNION ALL SELECT 'system_menu', COUNT(*) FROM system_menu
UNION ALL SELECT 'system_user_role', COUNT(*) FROM system_user_role
UNION ALL SELECT 'system_role_menu', COUNT(*) FROM system_role_menu;

-- 验证 admin 的权限查询
SELECT DISTINCT sm.permission
FROM system_menu sm
JOIN system_role_menu srm ON sm.id = srm.menu_id
JOIN system_user_role ur ON srm.role_id = ur.role_id
WHERE ur.user_id = 1;
