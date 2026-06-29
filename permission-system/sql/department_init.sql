-- ==================== 部门表 ====================
CREATE TABLE IF NOT EXISTS `sys_department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `dept_code` VARCHAR(64) NOT NULL COMMENT '部门编码',
    `dept_name` VARCHAR(128) NOT NULL COMMENT '部门名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '上级部门ID',
    `manager_username` VARCHAR(64) DEFAULT NULL COMMENT '部门经理用户名',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` VARCHAR(1) DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dept_code` (`dept_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ==================== 用户部门关联表 ====================
CREATE TABLE IF NOT EXISTS `sys_user_department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `department_id` BIGINT NOT NULL COMMENT '部门ID',
    `is_primary` TINYINT DEFAULT 0 COMMENT '是否主部门：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_dept` (`username`, `department_id`),
    KEY `idx_username` (`username`),
    KEY `idx_department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户部门关联表';

-- ==================== 初始化部门数据 ====================
INSERT INTO `sys_department` (`dept_code`, `dept_name`, `parent_id`, `manager_username`, `status`, `sort_order`, `remark`) VALUES
('TECH', '技术部', 0, 'manager', 1, 1, '技术研发部门'),
('HR', '人力资源部', 0, 'hr_01', 1, 2, '人力资源管理部门'),
('FINANCE', '财务部', 0, 'finance_manager', 1, 3, '财务管理部门'),
('SALES', '销售部', 0, 'sales_manager', 1, 4, '销售部门'),
('OPS', '运营部', 0, 'ops_manager', 1, 5, '运营部门');

-- ==================== 初始化用户部门关联 ====================
-- 假设 manager 用户ID为 1，hr_01 用户ID为 2
INSERT INTO `sys_user_department` (`user_id`, `username`, `department_id`, `is_primary`) VALUES
(1, 'manager', 1, 1),
(2, 'hr_01', 2, 1);
