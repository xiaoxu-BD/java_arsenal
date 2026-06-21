-- ============================================================
-- 流程设计器菜单
-- 依赖 workflow_init.sql 已建好工作流父菜单 (id=100)
-- ============================================================

-- 1) 菜单：流程设计
INSERT INTO `system_menu`
    (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`visible`,`deleted`)
VALUES
    (111, '流程设计', 'workflow:designer', 2, 5, 100,
     '/workflow/designer', 'icon-settings',
     '@/views/workflow/designer/index.vue', '0', '0')
ON DUPLICATE KEY UPDATE
    `name`=VALUES(`name`),
    `permission`=VALUES(`permission`),
    `path`=VALUES(`path`),
    `icon`=VALUES(`icon`),
    `component`=VALUES(`component`),
    `deleted`='0';

-- 2) 授予超管 (role_id=1)
INSERT INTO `system_role_menu` (`role_id`,`menu_id`,`deleted`) VALUES
    (1, 111, '0')
ON DUPLICATE KEY UPDATE `deleted`='0';
