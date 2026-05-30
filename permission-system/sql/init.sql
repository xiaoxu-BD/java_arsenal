-- ================================================================
--  权限管理系统 - 数据库初始化脚本
--  数据库名: 2026mysql_ds (与 application.yml 一致)
-- ================================================================

CREATE DATABASE IF NOT EXISTS `2026mysql_ds` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `2026mysql_ds`;

-- ================================================================
--  1. 用户表 (system_users)
-- ================================================================
CREATE TABLE `system_users` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`      VARCHAR(64)  NOT NULL                COMMENT '用户名',
    `password`      VARCHAR(128) NOT NULL                COMMENT '密码(BCrypt加密)',
    `nickname`      VARCHAR(64)  DEFAULT NULL            COMMENT '昵称',
    `remark`        VARCHAR(256) DEFAULT NULL            COMMENT '备注',
    `dept_id`       BIGINT       DEFAULT NULL            COMMENT '部门ID',
    `post_ids`      VARCHAR(256) DEFAULT NULL            COMMENT '岗位ID列表(JSON数组)',
    `email`         VARCHAR(128) DEFAULT NULL            COMMENT '邮箱',
    `mobile`        VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `sex`           TINYINT      DEFAULT 0               COMMENT '性别(0-未知 1-男 2-女)',
    `avatar`        VARCHAR(256) DEFAULT NULL            COMMENT '头像URL',
    `status`        TINYINT      DEFAULT 0               COMMENT '状态(0-正常 1-停用)',
    `creator`       VARCHAR(64)  DEFAULT NULL            COMMENT '创建者',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       VARCHAR(64)  DEFAULT NULL            COMMENT '更新者',
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       VARCHAR(1)   DEFAULT '0'             COMMENT '删除标识(0-未删 1-已删)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='用户表';

-- ================================================================
--  2. 角色表 (system_role)
-- ================================================================
CREATE TABLE `system_role` (
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `name`                  VARCHAR(64)  NOT NULL                COMMENT '角色名称(如: 管理员)',
    `code`                  VARCHAR(64)  NOT NULL                COMMENT '角色编码(如: admin)',
    `sort`                  INT          DEFAULT 0               COMMENT '显示排序(越小越靠前)',
    `data_scope`            TINYINT      DEFAULT 1               COMMENT '数据权限范围(1-全部 2-本部门 3-本部门及以下 4-仅本人 5-自定义)',
    `data_scope_dept_ids`   VARCHAR(512) DEFAULT NULL            COMMENT '自定义数据范围的部门ID列表(JSON)',
    `status`                TINYINT      DEFAULT 0               COMMENT '状态(0-正常 1-停用)',
    `creator`               VARCHAR(64)  DEFAULT NULL            COMMENT '创建者',
    `create_time`           DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`               VARCHAR(64)  DEFAULT NULL            COMMENT '更新者',
    `update_time`           DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`               VARCHAR(1)   DEFAULT '0'             COMMENT '删除标识(0-未删 1-已删)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='角色表';

-- ================================================================
--  3. 菜单/权限表 (system_menu)
--     同时承载菜单(目录、菜单)和按钮权限
-- ================================================================
CREATE TABLE `system_menu` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `name`            VARCHAR(64)  NOT NULL                COMMENT '菜单名称',
    `permission`      VARCHAR(128) DEFAULT NULL            COMMENT '权限标识(如: system:user:list)',
    `type`            TINYINT      NOT NULL                COMMENT '类型(1-目录 2-菜单 3-按钮)',
    `sort`            INT          DEFAULT 0               COMMENT '排序(越小越靠前)',
    `parent_id`       BIGINT       DEFAULT 0               COMMENT '父菜单ID(0表示顶级)',
    `path`            VARCHAR(256) DEFAULT NULL            COMMENT '路由地址(如: /user)',
    `icon`            VARCHAR(128) DEFAULT NULL            COMMENT '菜单图标',
    `component`       VARCHAR(256) DEFAULT NULL            COMMENT '组件路径(如: @/views/user/index.vue)',
    `component_name`  VARCHAR(128) DEFAULT NULL            COMMENT '组件名称(用于缓存)',
    `status`          TINYINT      DEFAULT 0               COMMENT '状态(0-正常 1-停用)',
    `visible`         VARCHAR(1)   DEFAULT '0'             COMMENT '是否可见(0-显示 1-隐藏)',
    `keep_alive`      VARCHAR(1)   DEFAULT '0'             COMMENT '是否缓存(0-否 1-是)',
    `always_show`     VARCHAR(1)   DEFAULT '0'             COMMENT '是否总是显示(0-否 1-是)',
    `creator`         VARCHAR(64)  DEFAULT NULL            COMMENT '创建者',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`         VARCHAR(64)  DEFAULT NULL            COMMENT '更新者',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         VARCHAR(1)   DEFAULT '0'             COMMENT '删除标识(0-未删 1-已删)',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='菜单权限表';

-- ================================================================
--  4. 用户-角色关联表 (system_user_role)
--     多对多: 一个用户可以有多个角色
-- ================================================================
CREATE TABLE `system_user_role` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `user_id`     BIGINT NOT NULL                COMMENT '用户ID',
    `role_id`     BIGINT NOT NULL                COMMENT '角色ID',
    `status`      TINYINT      DEFAULT 0         COMMENT '状态(0-正常 1-停用)',
    `creator`     VARCHAR(64)  DEFAULT NULL      COMMENT '创建者',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     VARCHAR(64)  DEFAULT NULL      COMMENT '更新者',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     VARCHAR(1)   DEFAULT '0'       COMMENT '删除标识(0-未删 1-已删)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='用户角色关联表';

-- ================================================================
--  5. 角色-菜单关联表 (system_role_menu)
--     多对多: 一个角色可以有多个菜单/权限
-- ================================================================
CREATE TABLE `system_role_menu` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `role_id`     BIGINT NOT NULL                COMMENT '角色ID',
    `menu_id`     BIGINT NOT NULL                COMMENT '菜单ID',
    `status`      TINYINT      DEFAULT 0         COMMENT '状态(0-正常 1-停用)',
    `creator`     VARCHAR(64)  DEFAULT NULL      COMMENT '创建者',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     VARCHAR(64)  DEFAULT NULL      COMMENT '更新者',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     VARCHAR(1)   DEFAULT '0'       COMMENT '删除标识(0-未删 1-已删)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='角色菜单关联表';


-- ================================================================
--  初始数据
-- ================================================================

-- 超级管理员
-- 密码 123456 的 BCrypt 值，请先运行 PermissionApplication.main() 生成后替换下面的 password
INSERT INTO `system_users` (`id`, `username`, `password`, `nickname`, `status`, `deleted`)
VALUES (1, 'admin', '请替换为运行main()后控制台输出的encodedPassword', '超级管理员', 0, '0');

-- 角色
INSERT INTO `system_role` (`id`, `name`, `code`, `sort`, `data_scope`, `deleted`) VALUES
(1, '超级管理员', 'super_admin', 1, 1, '0'),
(2, '普通用户',   'user',        2, 5, '0');

-- 菜单 - 顶级目录
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `visible`, `deleted`) VALUES
(1, '系统管理', NULL,             1, 1, 0, '/system',  'icon-settings', NULL,                        '0', '0'),
(2, '首页',     NULL,             2, 0, 0, '/',        'icon-home',     '@/views/dashboard/index.vue', '0', '0');

-- 菜单 - 系统管理子菜单
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `visible`, `deleted`) VALUES
(3, '用户管理', NULL,              2, 1, 1, 'user',     'icon-user',       '@/views/user/index.vue', '0', '0'),
(4, '角色管理', NULL,              2, 2, 1, 'role',     'icon-user-group', '@/views/role/index.vue', '0', '0'),
(5, '菜单管理', NULL,              2, 3, 1, 'menu',     'icon-menu',       '@/views/menu/index.vue', '0', '0');

-- 按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `deleted`) VALUES
(6, '用户查询', 'system:user:query',  3, 1, 3, NULL, '0'),
(7, '用户新增', 'system:user:create', 3, 2, 3, NULL, '0'),
(8, '用户修改', 'system:user:update', 3, 3, 3, NULL, '0'),
(9, '用户删除', 'system:user:delete', 3, 4, 3, NULL, '0'),
(10,'角色查询', 'system:role:query',  3, 1, 4, NULL, '0'),
(11,'角色新增', 'system:role:create', 3, 2, 4, NULL, '0'),
(12,'角色修改', 'system:role:update', 3, 3, 4, NULL, '0'),
(13,'角色删除', 'system:role:delete', 3, 4, 4, NULL, '0'),
(14,'菜单查询', 'system:menu:query',  3, 1, 5, NULL, '0'),
(15,'菜单新增', 'system:menu:create', 3, 2, 5, NULL, '0'),
(16,'菜单修改', 'system:menu:update', 3, 3, 5, NULL, '0'),
(17,'菜单删除', 'system:menu:delete', 3, 4, 5, NULL, '0');

-- 用户-角色关联 (admin 拥有 super_admin 角色)
INSERT INTO `system_user_role` (`user_id`, `role_id`, `deleted`) VALUES (1, 1, '0');

-- 角色-菜单关联 (super_admin 拥有所有菜单)
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `deleted`) VALUES
(1, 1, '0'), (1, 2, '0'), (1, 3, '0'), (1, 4, '0'), (1, 5, '0'),
(1, 6, '0'), (1, 7, '0'), (1, 8, '0'), (1, 9, '0'),
(1,10, '0'), (1,11, '0'), (1,12, '0'), (1,13, '0'),
(1,14, '0'), (1,15, '0'), (1,16, '0'), (1,17, '0');
