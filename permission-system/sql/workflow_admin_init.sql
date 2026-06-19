-- ============================================================
-- 全部任务（管理员视角）菜单 + 授权
-- 依赖 workflow_init.sql 已建好工作流父菜单 (id=100)
-- ============================================================

-- 1) 菜单：全部任务，权限码 workflow:admin:list
INSERT INTO `system_menu`
    (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`visible`,`deleted`)
VALUES
    (110, '全部任务', 'workflow:admin:list', 2, 4, 100,
     '/workflow/admin', 'icon-list',
     '@/views/workflow/admin/index.vue', '0', '0')
ON DUPLICATE KEY UPDATE
    `name`=VALUES(`name`),
    `permission`=VALUES(`permission`),
    `path`=VALUES(`path`),
    `icon`=VALUES(`icon`),
    `component`=VALUES(`component`),
    `deleted`='0';

-- 2) 仅授予超管 (role_id=1)
INSERT INTO `system_role_menu` (`role_id`,`menu_id`,`deleted`) VALUES
    (1, 110, '0')
ON DUPLICATE KEY UPDATE `deleted`='0';
