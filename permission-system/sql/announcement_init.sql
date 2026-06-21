-- 系统公告表
CREATE TABLE IF NOT EXISTS `sys_announcement` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `content` TEXT COMMENT '正文',
  `type` VARCHAR(20) DEFAULT 'INFO' COMMENT '类型: INFO-通知 WARNING-警告 UPDATE-更新',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0-草稿 1-已发布 2-已撤回',
  `publisher` VARCHAR(64) COMMENT '发布人',
  `publish_time` DATETIME COMMENT '发布时间',
  `create_time` DATETIME,
  `update_time` DATETIME,
  `deleted` VARCHAR(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告';

-- 公告已读记录表
CREATE TABLE IF NOT EXISTS `sys_announcement_read` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `announcement_id` BIGINT NOT NULL COMMENT '公告 ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `read_time` DATETIME COMMENT '阅读时间',
  UNIQUE KEY `uk_announcement_user` (`announcement_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告已读记录';
