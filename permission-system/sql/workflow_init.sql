-- ============================================================
-- 工作流业务表 + 菜单数据
-- 表名与用户实际表保持一致：approve_leave / fulfillment_order
-- 审计字段使用 create_time / update_time / deleted (VARCHAR(1))
-- ============================================================

CREATE TABLE IF NOT EXISTS `approve_leave` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT COMMENT '用户ID',
    `user_name` VARCHAR(64) COMMENT '用户名',
    `leave_reason` VARCHAR(500) COMMENT '请假原因',
    `leave_day` DECIMAL(5,1) COMMENT '请假天数',
    `begin_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `process_instance_id` VARCHAR(64) COMMENT '流程实例ID',
    `leave_type` VARCHAR(32) COMMENT '请假类型',
    `identifier` VARCHAR(64) COMMENT '唯一标识（UUID）',
    `status` VARCHAR(32) DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PROCESSING/APPROVED/REJECTED/CANCELLED',
    `creator` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` VARCHAR(1) DEFAULT '0' COMMENT '逻辑删除：0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_user_name` (`user_name`),
    INDEX `idx_identifier` (`identifier`),
    INDEX `idx_process_instance_id` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假审批表';

CREATE TABLE IF NOT EXISTS `fulfillment_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号',
    `title` VARCHAR(200) COMMENT '标题',
    `description` VARCHAR(1000) COMMENT '描述',
    `status` VARCHAR(32) DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PROCESSING/APPROVED/REJECTED/CANCELLED',
    `process_inst_id` VARCHAR(64) COMMENT '流程实例ID',
    `amount` DECIMAL(12,2) COMMENT '金额',
    `applicant` VARCHAR(64) COMMENT '申请人',
    `remark` VARCHAR(500) COMMENT '备注',
    `creator` VARCHAR(64) COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` VARCHAR(1) DEFAULT '0' COMMENT '逻辑删除：0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_order_no` (`order_no`),
    INDEX `idx_applicant` (`applicant`),
    INDEX `idx_status` (`status`),
    INDEX `idx_process_inst_id` (`process_inst_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='履约订单表';

-- ============================================================
-- 工作流菜单数据
-- ============================================================

DELETE FROM `system_menu` WHERE `id` >= 100 AND `id` <= 109;
DELETE FROM `system_role_menu` WHERE `menu_id` >= 100 AND `menu_id` <= 109;

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `visible`, `deleted`) VALUES
(100, '工作流管理', NULL, 1, 2, 0, '/workflow', 'icon-loop', NULL, '0', '0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `visible`, `deleted`) VALUES
(101, '审批任务',   'workflow:task:list',    2, 1, 100, '/workflow/tasks',       'icon-check-circle', '@/views/workflow/tasks/index.vue',       '0', '0'),
(102, '请假管理',   'workflow:leave:list',   2, 2, 100, '/workflow/leave',       'icon-calendar',     '@/views/workflow/leave/index.vue',       '0', '0'),
(103, '履约单管理', 'workflow:fulfill:list', 2, 3, 100, '/workflow/fulfillment', 'icon-file',         '@/views/workflow/fulfillment/index.vue', '0', '0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `deleted`) VALUES
(104, '审批通过',   'workflow:task:approve',  3, 1, 101, NULL, '0'),
(105, '审批驳回',   'workflow:task:reject',   3, 2, 101, NULL, '0'),
(106, '请假新增',   'workflow:leave:create',  3, 1, 102, NULL, '0'),
(107, '请假删除',   'workflow:leave:delete',  3, 2, 102, NULL, '0'),
(108, '履约单新增', 'workflow:fulfill:create', 3, 1, 103, NULL, '0'),
(109, '履约单删除', 'workflow:fulfill:delete', 3, 2, 103, NULL, '0');

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `deleted`) VALUES
(1, 100, '0'),
(1, 101, '0'), (1, 102, '0'), (1, 103, '0'),
(1, 104, '0'), (1, 105, '0'), (1, 106, '0'), (1, 107, '0'),
(1, 108, '0'), (1, 109, '0');
