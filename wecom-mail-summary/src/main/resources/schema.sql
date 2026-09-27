-- H2(MySQL 兼容模式)初始化脚本,与 docs/schema-mysql.sql 的 MySQL 正式 DDL 保持一致
-- 0. 企业微信群列表缓存表(仅 roomId:群名称接口无法提供,由管理员人工确认填写)
CREATE TABLE IF NOT EXISTS t_e_wechat_room (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    room_id     VARCHAR(64) NOT NULL,
    sync_time   TIMESTAMP   NULL,
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_wrm_room_id ON t_e_wechat_room (room_id);

CREATE TABLE IF NOT EXISTS t_e_cargo_declare_info (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    room_id     VARCHAR(64)  NOT NULL,
    group_name  VARCHAR(128) NOT NULL,
    send_time   TIME         NOT NULL,
    valid       TINYINT      NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_room_id ON t_e_cargo_declare_info (room_id);

CREATE TABLE IF NOT EXISTS t_e_client_email (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    declare_info_id BIGINT       NOT NULL,
    email_address   VARCHAR(128) NOT NULL,
    email_type      VARCHAR(8)   NOT NULL,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_declare_info ON t_e_client_email (declare_info_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_group_type_email ON t_e_client_email (declare_info_id, email_type, email_address);

CREATE TABLE IF NOT EXISTS t_e_enterprise_wechat_record (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    msg_id      VARCHAR(128) NOT NULL,
    action      VARCHAR(16)  NOT NULL DEFAULT 'send',
    from_user   VARCHAR(64)  NOT NULL,
    room_id     VARCHAR(64)  NOT NULL,
    msg_time    TIMESTAMP    NOT NULL,
    msg_type    VARCHAR(16)  NOT NULL,
    msg_content TEXT         NOT NULL,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_msg_id ON t_e_enterprise_wechat_record (msg_id);
CREATE INDEX IF NOT EXISTS idx_room_msgtime ON t_e_enterprise_wechat_record (room_id, msg_time);

CREATE TABLE IF NOT EXISTS t_e_wechat_mail_send_record (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    declare_info_id BIGINT        NOT NULL,
    room_id         VARCHAR(64)   NOT NULL,
    send_date       DATE          NOT NULL,
    round_no        INT           NOT NULL DEFAULT 1,
    send_status     VARCHAR(16)   NOT NULL,
    send_time       TIMESTAMP     NULL,
    error_message   VARCHAR(1024) NULL,
    retry_count     INT           NOT NULL DEFAULT 0,
    message_count   INT           NOT NULL DEFAULT 0,
    to_addresses    VARCHAR(1024) NOT NULL,
    cc_addresses    VARCHAR(1024) NULL,
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_group_date_round ON t_e_wechat_mail_send_record (declare_info_id, send_date, round_no);
CREATE INDEX IF NOT EXISTS idx_room_date ON t_e_wechat_mail_send_record (room_id, send_date);

CREATE TABLE IF NOT EXISTS t_e_wechat_mail_send_detail (
    id             BIGINT   NOT NULL AUTO_INCREMENT,
    send_record_id BIGINT   NOT NULL,
    message_id     BIGINT   NOT NULL,
    create_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_record_message ON t_e_wechat_mail_send_detail (send_record_id, message_id);
CREATE INDEX IF NOT EXISTS idx_message ON t_e_wechat_mail_send_detail (message_id);
