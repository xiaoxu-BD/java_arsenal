-- ============================================================
-- 付款单菜单 + 权限 初始化
-- 前置：已执行 init.sql
-- ============================================================

-- 1. 添加付款单菜单（顶级菜单）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `visible`, `deleted`)
VALUES
    (112, '我的付款单', 'pay:list', 2, 3, 0,
     '/pay', 'icon-file',
     '@/views/pay/index.vue', '0', '0')
ON DUPLICATE KEY UPDATE
    `name`=VALUES(`name`),
    `permission`=VALUES(`permission`),
    `path`=VALUES(`path`),
    `icon`=VALUES(`icon`),
    `component`=VALUES(`component`),
    `deleted`='0';
-- 2. 按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `deleted`) VALUES
(113, '付款单查询', 'pay:query',   3, 1, 112, NULL, '0'),
(114, '发起支付',   'pay:pay',     3, 2, 112, NULL, '0'),
(115, '取消订单',   'pay:cancel',  3, 3, 112, NULL, '0')
ON DUPLICATE KEY UPDATE `deleted`='0';

-- 3. super_admin 角色绑定
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `deleted`) VALUES
(1, 112, '0'),
(1, 113, '0'),
(1, 114, '0'),
(1, 115, '0')
ON DUPLICATE KEY UPDATE `deleted`='0';
