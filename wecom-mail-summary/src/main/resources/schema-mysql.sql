-- ============================================================
-- 企业微信群消息汇总邮件系统 MySQL 正式 DDL (MySQL 8.x, InnoDB, utf8mb4)
-- 与 src/main/resources/schema.sql(H2 演示用)保持同构
-- 使用方式:先建库 CREATE DATABASE wecom_mail DEFAULT CHARACTER SET utf8mb4;
--          再执行本脚本,然后以 --spring.profiles.active=mysql 启动应用
-- ============================================================

-- 0. 企业微信群列表缓存表(仅 roomId:群名称接口无法提供,由管理员人工确认填写)
CREATE TABLE IF NOT EXISTS t_e_wechat_room (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    room_id     VARCHAR(64) NOT NULL COMMENT '群ID',
    sync_time   DATETIME    NULL COMMENT '最近一次同步时间',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wrm_room_id (room_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '企业微信群列表缓存表';

-- 1. 微信群配置表
CREATE TABLE IF NOT EXISTS t_e_cargo_declare_info (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    room_id     VARCHAR(64)  NOT NULL COMMENT '企业微信群唯一标识',
    group_name  VARCHAR(128) NOT NULL COMMENT '群名称',
    send_time   TIME         NOT NULL COMMENT '每日固定发送时间',
    valid       TINYINT      NOT NULL DEFAULT 1 COMMENT '1有效 0逻辑删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_room_id (room_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '微信群配置表';

-- 2. 邮箱配置表
CREATE TABLE IF NOT EXISTS t_e_client_email (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    declare_info_id BIGINT       NOT NULL COMMENT '群配置ID',
    email_address   VARCHAR(128) NOT NULL COMMENT '邮箱地址',
    email_type      VARCHAR(8)   NOT NULL COMMENT 'TO/CC',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_declare_info (declare_info_id),
    UNIQUE KEY uk_group_type_email (declare_info_id, email_type, email_address)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '邮箱配置表';

-- 3. 企业微信群消息表
CREATE TABLE IF NOT EXISTS t_e_enterprise_wechat_record (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    msg_id      VARCHAR(128) NOT NULL COMMENT '企业微信msgid,MQ消费幂等键',
    action      VARCHAR(16)  NOT NULL DEFAULT 'send' COMMENT '动作,当前仅send',
    from_user   VARCHAR(64)  NOT NULL COMMENT '发送人',
    room_id     VARCHAR(64)  NOT NULL COMMENT '群ID',
    msg_time    DATETIME(3)  NOT NULL COMMENT '消息时间(msgtime毫秒时间戳转换)',
    msg_type    VARCHAR(16)  NOT NULL COMMENT '消息类型,当前仅text',
    msg_content TEXT         NOT NULL COMMENT '消息正文 text.content',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_msg_id (msg_id),
    KEY idx_room_msgtime (room_id, msg_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '企业微信群消息表';

-- 4. 邮件发送记录表
CREATE TABLE IF NOT EXISTS t_e_wechat_mail_send_record (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    declare_info_id BIGINT        NOT NULL COMMENT '群配置ID',
    room_id         VARCHAR(64)   NOT NULL COMMENT '群ID(冗余快照)',
    send_date       DATE          NOT NULL COMMENT '业务日期',
    round_no        INT           NOT NULL DEFAULT 1 COMMENT '第几轮(1=正点日报,>1=迟到消息补充轮)',
    send_status     VARCHAR(16)   NOT NULL COMMENT 'WAITING/SENDING/SUCCESS/FAIL',
    send_time       DATETIME      NULL COMMENT '实际发送成功时间',
    error_message   VARCHAR(1024) NULL COMMENT '失败原因(截断1024)',
    retry_count     INT           NOT NULL DEFAULT 0 COMMENT '已重试次数',
    message_count   INT           NOT NULL DEFAULT 0 COMMENT '本封邮件消息数(冗余)',
    to_addresses    VARCHAR(1024) NOT NULL COMMENT '收件人快照,分号分隔',
    cc_addresses    VARCHAR(1024) NULL COMMENT '抄送快照,分号分隔',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_date_round (declare_info_id, send_date, round_no),
    KEY idx_room_date (room_id, send_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '邮件发送记录表';

-- 5. 邮件发送明细表
CREATE TABLE IF NOT EXISTS t_e_wechat_mail_send_detail (
    id             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    send_record_id BIGINT   NOT NULL COMMENT '发送记录ID',
    message_id     BIGINT   NOT NULL COMMENT '消息ID',
    create_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_record_message (send_record_id, message_id),
    KEY idx_message (message_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '邮件发送明细表';
