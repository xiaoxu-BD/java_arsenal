-- ================================================================
--  公告管理菜单 + 权限 初始化
--  前置：已执行 init.sql + announcement_init.sql
-- ================================================================

-- 1. 添加公告管理菜单（系统管理子菜单，id 从 20 开始避免冲突）
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `visible`, `deleted`) VALUES
(20, '公告管理', NULL, 2, 4, 1, 'announcement', 'icon-notification', '@/views/announcement/index.vue', '0', '0');

-- 2. 添加公告按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `deleted`) VALUES
(21, '公告查询', 'system:announcement:query',   3, 1, 20, NULL, '0'),
(22, '公告新增', 'system:announcement:create',  3, 2, 20, NULL, '0'),
(23, '公告修改', 'system:announcement:update',  3, 3, 20, NULL, '0'),
(24, '公告删除', 'system:announcement:delete',  3, 4, 20, NULL, '0');

-- 3. super_admin 角色绑定公告菜单和权限
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `deleted`) VALUES
(1, 20, '0'),
(1, 21, '0'),
(1, 22, '0'),
(1, 23, '0'),
(1, 24, '0');
