-- 幂等建表, 每次启动都执行, 已存在则跳过
CREATE TABLE IF NOT EXISTS demo_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(20)   NOT NULL COMMENT '姓名',
    phone       VARCHAR(11)   NOT NULL COMMENT '手机号',
    email       VARCHAR(50)   NULL COMMENT '邮箱(可选)',
    dept_name   VARCHAR(20)   NOT NULL COMMENT '部门',
    salary      DECIMAL(10,2) NOT NULL COMMENT '薪资',
    hire_date   DATE          NOT NULL COMMENT '入职日期',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
    UNIQUE KEY uk_phone (phone)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Excel 导入演示用户表';
