-- ============================================================
-- 工作流 ↔ RBAC 集成
-- 设计原则：BPMN 中 flowable:candidateGroups 的字符串
--          == system_role.code（区分大小写）
-- 当前涉及候选组：ROLE_MANAGER / ROLE_DIRECTOR / ROLE_VP / ROLE_HR
-- 前置依赖：workflow_init.sql（提供菜单 100~109）
-- ============================================================

-- 1) 新增审批岗角色（id 用 10+ 留出业务扩展空间）
INSERT INTO `system_role` (`id`,`name`,`code`,`sort`,`data_scope`,`deleted`) VALUES
(10, '部门主管', 'ROLE_MANAGER',  10, 5, '0'),
(11, '总监',     'ROLE_DIRECTOR', 11, 5, '0'),
(12, '副总',     'ROLE_VP',       12, 5, '0'),
(13, 'HR专员',   'ROLE_HR',       13, 5, '0')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `deleted` = '0';

-- 2) 角色 ↔ 工作流菜单授权（菜单 id 100~109 来自 workflow_init.sql）
--    审批岗：工作流 + 审批任务 + 通过 + 驳回
--    普通员工(role_id=2)：工作流 + 请假 + 履约 提交入口
DELETE FROM `system_role_menu`
 WHERE `role_id` IN (2,10,11,12,13)
   AND `menu_id` BETWEEN 100 AND 109;

INSERT INTO `system_role_menu` (`role_id`,`menu_id`,`deleted`) VALUES
-- 主管
(10,100,'0'),(10,101,'0'),(10,104,'0'),(10,105,'0'),
-- 总监
(11,100,'0'),(11,101,'0'),(11,104,'0'),(11,105,'0'),
-- 副总
(12,100,'0'),(12,101,'0'),(12,104,'0'),(12,105,'0'),
-- HR
(13,100,'0'),(13,101,'0'),(13,104,'0'),(13,105,'0'),
-- 普通用户：可见工作流父菜单 + 请假管理 + 履约单管理（含按钮）
(2,100,'0'),(2,102,'0'),(2,103,'0'),
(2,106,'0'),(2,107,'0'),(2,108,'0'),(2,109,'0');

-- 3) 测试用户：复用 admin 当前密码哈希（admin 密码是什么，新用户就是什么）
INSERT IGNORE INTO `system_users` (`username`,`password`,`nickname`,`deleted`)
SELECT 'manager',  `password`, '主管账号', '0' FROM `system_users` WHERE `username` = 'admin'
UNION ALL
SELECT 'director', `password`, '总监账号', '0' FROM `system_users` WHERE `username` = 'admin'
UNION ALL
SELECT 'vp',       `password`, '副总账号', '0' FROM `system_users` WHERE `username` = 'admin'
UNION ALL
SELECT 'hr',       `password`, 'HR账号',   '0' FROM `system_users` WHERE `username` = 'admin'
UNION ALL
SELECT 'employee', `password`, '普通员工', '0' FROM `system_users` WHERE `username` = 'admin';

-- 4) 用户 ↔ 角色 绑定
INSERT INTO `system_user_role` (`user_id`,`role_id`,`deleted`)
SELECT u.id, 10, '0' FROM `system_users` u WHERE u.`username` = 'manager'
   AND NOT EXISTS (SELECT 1 FROM `system_user_role` x WHERE x.user_id = u.id AND x.role_id = 10)
UNION ALL
SELECT u.id, 11, '0' FROM `system_users` u WHERE u.`username` = 'director'
   AND NOT EXISTS (SELECT 1 FROM `system_user_role` x WHERE x.user_id = u.id AND x.role_id = 11)
UNION ALL
SELECT u.id, 12, '0' FROM `system_users` u WHERE u.`username` = 'vp'
   AND NOT EXISTS (SELECT 1 FROM `system_user_role` x WHERE x.user_id = u.id AND x.role_id = 12)
UNION ALL
SELECT u.id, 13, '0' FROM `system_users` u WHERE u.`username` = 'hr'
   AND NOT EXISTS (SELECT 1 FROM `system_user_role` x WHERE x.user_id = u.id AND x.role_id = 13)
UNION ALL
SELECT u.id, 2,  '0' FROM `system_users` u WHERE u.`username` = 'employee'
   AND NOT EXISTS (SELECT 1 FROM `system_user_role` x WHERE x.user_id = u.id AND x.role_id = 2);
