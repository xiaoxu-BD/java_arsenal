# 企业微信群消息自动汇总发送邮件系统 — 后端设计文档

> 阶段说明：前端原型（wecom_mail_mock_demo.html）已确认，本文档为后端设计稿，**不含任何 Java 实现**。
> 待数据库设计与业务流程确认后，再进入编码阶段。
>
> 阅读顺序：业务需求 → 状态流转 → 数据库表 → MQ 消费 → 幂等 → 定时任务 → 邮件发送 → 发送记录 → 异常重试 → 多实例并发 → 待确认决策点。
>
> **实现进展（2026-09-27）**：持久层为 **MyBatis-Plus**（BaseMapper + 注解 SQL，替代原设想的 JPA）；Mock 已全部移除——群列表改为**仅 roomId**（url 未配置=后端写死 Mock，已配置=真实 HTTP，落库新表 `t_e_wechat_room`；群名称接口无法提供，由管理员配置时人工确认填写），邮件走真实 SMTP，演示种子数据删除，管理页随应用部署（static/index.html）；消息接入为 `POST /api/messages`（联调/补录），MQ 产品确定后由 Listener 回调同一消费逻辑。运行方式见项目 README。

---

## 1. 业务需求回顾

### 1.1 系统定位

接收其他开发组通过 MQ 推送的企业微信群消息，按群配置的「收件人 + 抄送人 + 每日固定发送时间」，在每天指定时刻将该群**尚未成功发送**的消息汇总成一封邮件发出，并留痕发送结果。

### 1.2 核心业务规则（来自原型确认稿）

| # | 规则 | 备注 |
|---|------|------|
| R1 | 只处理 `msgtype = text` 的 MQ 消息，邮件正文取 `text.content` | 其他类型直接忽略 |
| R2 | `roomid` 为空的消息直接忽略（本系统只处理群消息） | 单聊不是群消息 |
| R3 | 只处理「已配置且有效」的群 | 未配置的群如何处理见决策点 D2 |
| R4 | 每个群独立配置：roomId、群名称、TO 收件人（多个）、CC 抄送人（多个）、每日固定发送时间 | 群与群之间完全独立 |
| R5 | 到达发送时间后：查询该群**尚未成功发送**的消息 → 汇总 → 发送 → 记录结果 | 「尚未成功发送」判定见 §8.2 |
| R6 | 发送失败需要重试，并保留失败原因 | 重试策略见 §9 |
| R7 | 一封邮件可能包含多条消息；「某条消息是否已成功发出」以「包含它的邮件是否 SUCCESS」为准 | 这就是不把 send_status 放在消息表上的原因 |
| R8 | roomId 全局唯一，新增时从其他组提供的 HTTP 群列表接口拉取并过滤已配置群 | 后端做代理 + 缓存 |
| R9 | 发送记录含 4 种状态：WAITING / SENDING / SUCCESS / FAIL，FAIL 展示失败原因 | 与原型一致 |

### 1.3 角色与用例

- **管理员**：维护群配置（增/改/删）、维护邮件配置（TO/CC/发送时间）、查看今日消息、查看发送记录与发送详情。
- **MQ 消费者**（系统内部）：消费群消息，校验、幂等入库。
- **定时任务**（系统内部）：到点触发汇总发送。
- **上游系统**（其他开发组）：推送 MQ 消息、提供群列表 HTTP 接口。

---

## 2. 总体架构与数据流

```
                    ┌──────────────────────────────────────────────────┐
                    │                    本系统 (Spring Boot)            │
                    │                                                  │
 其他开发组 ──MQ──►  │  ┌────────────┐   INSERT(幂等)   ┌─────────────┐  │
 (企业微信群消息)    │  │ MQ Consumer │ ───────────────► │  MySQL 消息表 │  │
                    │  └────────────┘                   └─────────────┘  │
                    │                                          ▲        │
 其他开发组 ──HTTP──► │  ┌────────────┐   读写配置         │        │
 (群列表接口)        │  │ Admin API  │ ──► 群配置表/邮箱表   │        │
                    │  │ (管理后台)  │                  查询"未成功发送"  │
 管理员 ──浏览器───► │  └────────────┘                       │        │
                    │                                         │        │
                    │  ┌────────────┐   创建WAITING/认领SENDING │        │
                    │  │ 定时任务    │ ───────────────────────►│        │
                    │  │ (每分钟扫描)│                   ┌───────┴────┐  │
                    │  └────────────┘                   │ 发送记录/明细 │  │
                    │        │                          └────────────┘  │
                    │        ▼                                          │
                    │  ┌────────────┐  SMTP                             │
                    │  │ Mail Sender│ ─────────────► 企业邮箱服务器        │
                    │  └────────────┘                                  │
                    └──────────────────────────────────────────────────┘
```

模块划分（Spring Boot 单应用内分包，不做微服务）：

| 模块 | 职责 |
|------|------|
| `consumer` | MQ 监听、消息校验、幂等入库 |
| `scheduler` | 每分钟扫描、触发/补发/重试/超时兜底 |
| `sender` | 查询未发送消息、组装邮件、SMTP 发送、结果回写 |
| `admin` | 管理后台 REST API（对应原型全部按钮） |
| `common` | 实体、Mapper、枚举、工具 |

---

## 3. 核心状态流转

### 3.1 发送记录状态机（t_e_wechat_mail_send_record.send_status）

```
                 定时扫描命中：到达发送时间 且 今日无记录
                 (INSERT ... UK(declare_info_id, send_date) 防重)
      ┌─────────┐
      │ WAITING │──────────────┐
      └─────────┘              │ CAS 认领：
           ▲                   │ UPDATE status='SENDING'
           │ 认领失败(被抢/已终态) │   WHERE status IN ('WAITING','FAIL')
      ┌────┴─────┐             │   [affected=0 则放弃，防多实例并发]
      │  放弃本次 │             ▼
      └─────────┘        ┌─────────┐
                         │ SENDING │──────── 实例宕机/卡死 >10min ──┐
                         └────┬────┘   (超时兜底扫描置为 FAIL)      │
                    SMTP 成功 │                                │
                    ┌────────┴───┐   SMTP 失败/超时               │
                    ▼            ▼                               ▼
              ┌─────────┐   ┌────────┐  退避时间到 & retry_count<3  ┌────────┐
              │ SUCCESS │   │  FAIL  │ ────────再次 CAS 认领──────►│ SENDING│
              └─────────┘   └────────┘                             └────────┘
               (终态)         │    ▲
                             │    └── retry_count ≥ 3 → 终态 FAIL，触发告警
                             └── 今日不再自动重试时，次日邮件会兜底携带其未送达消息
```

状态语义（与原型 UI 一致）：

| 状态 | 语义 | 进入条件 |
|------|------|----------|
| WAITING | 记录已创建，等待发送线程认领 | 定时扫描到点创建 |
| SENDING | 某实例正在发送中 | CAS 认领成功 |
| SUCCESS | 邮件发送成功，`send_time` 为实际成功时间 | SMTP 成功 |
| FAIL | 发送失败，`error_message` 记录原因；可被重试再次认领 | SMTP 异常 / 超时兜底 |

### 3.2 消息生命周期（t_e_enterprise_wechat_record）

```
MQ 到达 → 校验(msgtype=text, roomid非空, 群已配置) → 幂等 INSERT(msg_id UK)
   │重复 msg_id → ack 忽略（已处理过）
   ▼
 已入库(待发送)
   │ 被某次发送选中 → 写入 send_detail
   ▼
 已被邮件引用 ──► 该邮件 SUCCESS = 消息已送达(终态，不再参与后续汇总)
              └► 该邮件 FAIL   = 消息仍属"未成功发送"，参与重试/次日汇总
```

要点：消息表本身**没有发送状态字段**；「是否已成功发送」永远通过明细表关联 SUCCESS 记录动态判定（R7）。

---

## 4. 数据库设计

数据库：MySQL 8.x，InnoDB，utf8mb4。约定所有表带 `create_time` / `update_time`（下方 DDL 略写公共字段）。

### 4.1 t_e_cargo_declare_info — 微信群配置表

```sql
CREATE TABLE t_e_cargo_declare_info (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    room_id     VARCHAR(64)  NOT NULL COMMENT '企业微信群唯一标识',
    group_name  VARCHAR(128) NOT NULL COMMENT '群名称',
    send_time   TIME         NOT NULL COMMENT '每日固定发送时间 HH:mm:ss',
    valid       TINYINT      NOT NULL DEFAULT 1 COMMENT '有效标志: 1有效 0删除(逻辑删除)',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_room_id (room_id)
) COMMENT '微信群配置表';
```

设计说明：

- `send_time` 放在本表而非邮箱表：它是**群级属性**（一个群一个时间点），原型中虽在「配置邮件」弹窗里设置，但归属仍是群维度。
- `uk_room_id` 与逻辑删除冲突（删除后无法重新添加同 roomId 的群）。处理策略采用**复活式更新**：新增时若命中 `valid=0` 的旧记录，改为 UPDATE 复活并覆盖配置，而不是 INSERT。
- 管理端删除走逻辑删除（`valid=0`），保证历史发送记录仍可关联查询。

### 4.2 t_e_client_email — 邮箱配置表

```sql
CREATE TABLE t_e_client_email (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    declare_info_id BIGINT      NOT NULL COMMENT '群配置ID → t_e_cargo_declare_info.id',
    email_address  VARCHAR(128) NOT NULL COMMENT '邮箱地址',
    email_type     VARCHAR(8)   NOT NULL COMMENT '类型: TO / CC',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_declare_info (declare_info_id),
    UNIQUE KEY uk_group_type_email (declare_info_id, email_type, email_address)
) COMMENT '邮箱配置表';
```

设计说明：

- 一个群可有多条 TO、多条 CC（原型中动态添加/删除）。
- 唯一键防止同类型下重复录入同一邮箱；同一邮箱同时配 TO 和 CC 允许存在（如业务上禁止可在 Service 层校验拦截）。
- 「配置邮件」保存语义：以群为单位整体覆盖（先删后插，事务内），与原型弹窗一次提交一致。

### 4.3 t_e_enterprise_wechat_record — 企业微信群消息表

```sql
CREATE TABLE t_e_enterprise_wechat_record (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    msg_id      VARCHAR(128) NOT NULL COMMENT '企业微信 msgid，MQ 幂等键',
    action      VARCHAR(16)  NOT NULL DEFAULT 'send' COMMENT '动作: send(当前仅此)',
    from_user   VARCHAR(64)  NOT NULL COMMENT '发送人',
    room_id     VARCHAR(64)  NOT NULL COMMENT '群ID',
    msg_time    DATETIME(3)  NOT NULL COMMENT '消息时间(由 msgtime 毫秒时间戳转换)',
    msg_type    VARCHAR(16)  NOT NULL COMMENT '消息类型(当前仅 text)',
    msg_content TEXT         NOT NULL COMMENT '消息正文 text.content',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_msg_id (msg_id),
    KEY idx_room_msgtime (room_id, msg_time)
) COMMENT '企业微信群消息表';
```

设计说明：

- `uk_msg_id` 是 **MQ 消费幂等的最终保证**（见 §5.2），不依赖 Redis。
- `msg_time` 由 MQ 消息里的毫秒时间戳转换存储，同时是汇总查询的时间轴；`idx_room_msgtime` 支撑「按群 + 时间范围查询未发送消息」。
- `msg_type` 保留字段而非写死 `text`，未来扩展图片/文件等类型时无需改表。
- 保留策略（清理 N 天前数据）见决策点 D8。

### 4.4 t_e_wechat_mail_send_record — 邮件发送记录表

```sql
CREATE TABLE t_e_wechat_mail_send_record (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    declare_info_id BIGINT        NOT NULL COMMENT '群配置ID',
    room_id         VARCHAR(64)   NOT NULL COMMENT '群ID(冗余快照，便于审计查询)',
    send_date       DATE          NOT NULL COMMENT '业务日期(哪一天的汇总)',
    send_status     VARCHAR(16)   NOT NULL COMMENT 'WAITING/SENDING/SUCCESS/FAIL',
    send_time       DATETIME      NULL COMMENT '实际发送(成功)时间',
    error_message   VARCHAR(1024) NULL COMMENT '失败原因(截断至1024)',
    retry_count     INT           NOT NULL DEFAULT 0 COMMENT '已重试次数',
    message_count   INT           NOT NULL DEFAULT 0 COMMENT '本封邮件包含消息数(冗余)',
    to_addresses    VARCHAR(1024) NOT NULL COMMENT '收件人快照,分号分隔',
    cc_addresses    VARCHAR(1024) NULL COMMENT '抄送快照,分号分隔',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_date (declare_info_id, send_date),
    KEY idx_room_date (room_id, send_date)
) COMMENT '邮件发送记录表';
```

设计说明：

- `uk_group_date(declare_info_id, send_date)`：**一个群一天最多一封邮件**，同时是定时任务防重复触发的天级幂等键。重试不新插记录，而是原记录状态流转 + `retry_count++`。
- `room_id`、`to_addresses`、`cc_addresses` 做快照冗余：群日后改名/改邮箱/被删除，历史记录仍能还原「当时发给谁」。
- `message_count` 冗余存储，避免发送记录列表页每行都 count 明细。

### 4.5 t_e_wechat_mail_send_detail — 邮件发送明细表

```sql
CREATE TABLE t_e_wechat_mail_send_detail (
    id             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    send_record_id BIGINT   NOT NULL COMMENT '发送记录ID → t_e_wechat_mail_send_record.id',
    message_id     BIGINT   NOT NULL COMMENT '消息ID → t_e_enterprise_wechat_record.id',
    create_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_record_message (send_record_id, message_id),
    KEY idx_message (message_id)
) COMMENT '邮件发送明细表(邮件↔消息 多对多留痕)';
```

设计说明：

- 回答「这一封邮件具体包含哪些群消息」——发送详情弹窗的数据来源。
- `idx_message` 支撑「某条消息是否已被 SUCCESS 邮件包含」的反查（§8.2 的 NOT EXISTS 子查询）。

### 4.6 表关系

```
t_e_cargo_declare_info (群配置) 1 ──── n t_e_client_email (TO/CC)
        │ 1
        │
        │ n (declare_info_id)
t_e_wechat_mail_send_record (发送记录) 1 ──── n t_e_wechat_mail_send_detail (明细) n ──── 1 t_e_enterprise_wechat_record (消息)
                                                                      (message_id)
t_e_enterprise_wechat_record.n(room_id) ──── 1 t_e_cargo_declare_info.room_id (逻辑外键,不建物理FK)
```

---

## 5. MQ 消费设计

### 5.1 消费校验链

```
收到消息
  │
  ├─ 反序列化 JSON 失败 ────────────────► 记录错误日志 + 告警计数，ack 丢弃（脏数据不重试）
  │
  ├─ msgtype != "text" ────────────────► 直接 ack 忽略（R1）
  │
  ├─ roomid 为空 ──────────────────────► 直接 ack 忽略（R2，单聊非群消息）
  │
  ├─ msgid 为空 ───────────────────────► 错误日志，ack 丢弃（无幂等键无法保证正确性）
  │
  ├─ roomid 未在配置表中(valid=1) ──────► ack 忽略 + 监控计数（决策点 D2）
  │
  └─ 全部通过 ──► 幂等 INSERT 消息表
```

要点：

- **所有忽略分支都 ack**（消费成功），不能 requeue——业务性过滤重试多少次结果都一样，反而会堵塞队列。
- 只有人为 INSERT 失败（DB 抖动等）才走 MQ 重试机制。
- `action` 字段当前只认 `send`；若上游存在 `recall`（撤回）语义需要同步删改已入库消息，见决策点 D5。
- `msgtime` 毫秒时间戳在消费端转 `DATETIME(3)` 存储。

### 5.2 幂等设计（核心）

**以数据库唯一约束为最终裁决，不依赖 Redis：**

```
INSERT INTO t_e_enterprise_wechat_record (msg_id, ...)
VALUES (...)
  │
  ├─ 成功 ──► 首次处理，ack
  │
  └─ DuplicateKeyException(uk_msg_id) ──► 已处理过，直接 ack 忽略
```

理由：MQ 至少一次投递（at-least-once）下重复消费是常态；Redis 去重（setnx + 过期）只能做**前置过滤优化**（减少 DB 压力，可选），宕机/过期后仍需 DB 兜底。唯一键 + 冲突吞并是最简单且零漏判的方案。

**事务边界**：INSERT 成功且事务提交后才 ack。若先 ack 后写库，宕机会丢消息。

### 5.3 异常与死信

| 异常 | 处理 |
|------|------|
| JSON 解析失败 / 业务字段非法 | 本地错误日志 + 告警计数，ack 丢弃 |
| DB 短暂不可用 | 抛异常交还 MQ 重试（RocketMQ 默认 16 次退避），超限进 DLQ |
| 上游突发流量 | 单条 INSERT 性能足够；如需加固可加 Redis 布隆过滤器前置去重（可选，非本期） |

MQ 产品未定（RocketMQ / RabbitMQ / Kafka 的重试与死信语义各不相同），见决策点 D1。

---

## 6. 定时任务设计

### 6.1 为什么是「每分钟扫描」而不是「每个群一个定点任务」

群配置可动态增删改，不能为每个群注册独立的定点 cron。采用**每分钟扫描 + 到点判断**：

```
cron: 0 * * * * ?   （每分钟整点触发）

扫描逻辑（单次执行）：
1) 补偿卡死：UPDATE record SET status=FAIL, error_message='发送超时,疑似实例宕机'
            WHERE status='SENDING' AND update_time < NOW()-10分钟
   ── 兜底实例发到一半宕机导致记录永久卡在 SENDING 的情况

2) 到点触发：SELECT 配置 WHERE valid=1 AND send_time <= 当前时间(仅今日)
   对每个命中的群：
     INSERT IGNORE INTO record (..., send_date=今天, status='WAITING')
     ── uk_group_date 已存在则忽略：天然实现"今天已建过任务不重复建"
     ── send_time <= now 而非 == now：服务 17:58~18:05 宕机重启后，18:05 扫描
        仍能补发，不漏群（停机补发能力）

3) 失败重试：SELECT record WHERE send_date=今天 AND status='FAIL'
            AND retry_count < 最大重试次数
            AND update_time <= NOW()-退避间隔(retry_count)
   命中的记录重新走 CAS 认领 → SENDING（见 §9）
```

### 6.2 执行流程（单群发送）

```
扫描线程创建 WAITING 记录
  │ 提交到发送线程池（群ID做队列分片，同群串行、异群并行）
  ▼
CAS 认领：
UPDATE record SET status='SENDING', update_time=NOW()
 WHERE declare_info_id=? AND send_date=? AND status IN ('WAITING','FAIL')
  │ affected = 0 → 已被其他实例认领 / 已终态 / 重试条件不满足 → 放弃
  ▼
查询"未成功发送"消息（§8.2 SQL）
  │
  ├─ 0 条 → 不发邮件，删除本条 WAITING 记录（或不创建，见决策点 D4），结束
  │
  └─ n 条 → 事务内写入 send_detail(n条) + 更新 message_count
           ▼
        组装固定模板邮件 → SMTP 发送（带超时）
           │
           ├─ 成功 → UPDATE status='SUCCESS', send_time=NOW()
           │
           └─ 异常 → UPDATE status='FAIL', error_message=摘要(截断1024),
                      retry_count=retry_count+1
```

---

## 7. 邮件发送设计

### 7.1 模板（固定，不做编辑器）

```
主题：【微信群消息日报】{group_name} {send_date:yyyy-MM-dd}

正文(HTML)：
  ┌────────────────────────────────────────────┐
  │ {group_name} {send_date} 消息汇总，共 {n} 条 │
  │                                            │
  │  09:12  张三：今天有3票货物需要申报            │
  │  11:26  李四：其中一票资料还没有补齐           │
  │  14:05  王五：资料已经补充完成                │
  │                                            │
  │ 收件人: cargo-a@company.com; ...             │
  │ 抄送:   manager-a@company.com; ...           │
  │ —— 本邮件由系统自动汇总发送，请勿直接回复       │
  └────────────────────────────────────────────┘
```

消息按 `msg_time` 升序排列；正文做 HTML 转义防止内容注入标签。

### 7.2 SMTP 配置要点

- Spring `JavaMailSender`，参数走配置中心（§12）。
- **必须设置超时**：`mail.smtp.connectiontimeout=5000`、`mail.smtp.timeout=10000`、`mail.smtp.writetimeout=10000`，防止 SMTP 挂起把记录长期卡在 SENDING。
- 发件人地址、SMTP 账号/授权码运维侧提供（决策点 D8 附带确认）。
- 发送 TO 为空（只有 CC）时直接判 FAIL，错误原因「未配置收件人」——发送前增加一次配置校验。

---

## 8. 发送记录与「未成功发送」判定

### 8.1 记录查询（对应原型「发送记录」弹窗 / 「查看详情」弹窗）

- 列表：按 send_date DESC 分页，展示发送时间、群名称、消息数量（冗余字段）、收件人快照、状态 Badge。
- 详情：记录主表字段 + 明细表 JOIN 消息表，展示本封邮件包含的每条消息（发送人 / 消息时间 / 消息内容）。

### 8.2 「尚未成功发送的消息」查询 SQL（R5 核心）

```sql
SELECT m.*
FROM t_e_enterprise_wechat_record m
WHERE m.room_id = #{roomId}
  AND m.msg_time <= NOW()
  AND NOT EXISTS (
        SELECT 1
        FROM t_e_wechat_mail_send_detail d
        JOIN t_e_wechat_mail_send_record r ON r.id = d.send_record_id
        WHERE d.message_id = m.id
          AND r.send_status = 'SUCCESS'
  )
ORDER BY m.msg_time ASC;
```

语义拆解：

1. `msg_time <= NOW()`：只汇总已到达的消息，**不预测未来**。
2. `NOT EXISTS (... SUCCESS)`：只要某条消息被任意一封 **SUCCESS** 的邮件包含过，即视为已送达，永不再发。
   - 注意子查询** deliberately 不过滤 declare_info_id**：群被删除重建后（新配置ID），旧 SUCCESS 邮件仍然算数，避免同一条消息重复发。
3. 该窗口天然覆盖三类场景：
   - 常规：当天 18:00 前的消息进当天邮件；
   - **迟到消息**：18:01 才到的消息，进入次日邮件（严格按自然日会永久丢失，见决策点 D3）；
   - **失败追补**：昨天邮件 FAIL 的消息，今天重发时自动包含。

---

## 9. 异常重试设计

| 项 | 方案 |
|----|------|
| 触发方式 | 不单独建重试 Job，复用 §6.1 第 3 步的分钟扫描 |
| 退避间隔 | 第1次重试 +5min，第2次 +15min，第3次 +30min（可配置） |
| 最大次数 | 3 次（首次发送 + 3 重试 = 最多 4 次尝试） |
| 重试范围 | 仅自动重试**当日**记录；昨日终态 FAIL 不再自动重试，其未送达消息由次日邮件自然兜底（§8.2 场景 3） |
| 重试内容 | 每次重试**重新执行 §8.2 查询**并重建明细——期间新到的消息一并带上，不丢不重 |
| 超限处理 | 置终态 FAIL，保留最后一次 error_message，输出告警（通道见决策点 D7） |
| 兜底 | SENDING 卡死 >10min 由分钟扫描强制置 FAIL 并允许重试（§6.1 第 1 步） |
| 人工干预 | 预留「手动重发」REST API（原型暂无按钮，后端先留接口，前端后续可加） |

---

## 10. 多实例并发设计

部署 N 个实例时，分钟扫描会同时触发，必须保证**一个群一天只有一封邮件、一次有效发送**。采用三层防线，DB 为强保证、外部锁仅为优化：

| 层 | 机制 | 作用 |
|----|------|------|
| L1 天级防重 | `uk_group_date(declare_info_id, send_date)` + INSERT IGNORE | 多实例同时创建任务，只有一条记录成功，其余忽略 |
| L2 执行互斥 | 状态 CAS：`UPDATE ... SET status='SENDING' WHERE ... AND status IN('WAITING','FAIL')`，affected=0 即放弃 | 多实例同时认领，只有一个 UPDATE 生效；**不依赖任何外部组件，是最终裁决** |
| L3 竞争优化（可选） | Redisson 分布式锁 `lock:mail:send:{declareInfoId}`，或调度层 ShedLock | 减少无效竞争和邮件重复组装的开销；没有它正确性也不受损 |

推荐组合：**L1 + L2 必做，L3 视部署规模选配**（已有 Redis 则顺手加锁，没有也不引入新组件）。

补充规则：

- SMTP 调用在 CAS 认领**之后**、锁范围之内/之外皆可，但结果回写只能由认领成功的实例执行。
- 认领后实例宕机 → 记录卡 SENDING → §6.1 超时兜底置 FAIL → 重试，邮件可能重发一次（SMTP 已发出但未回写）。这是「至少一次」语义下的可接受边界，收件方最多收到重复邮件，不会漏；如业务要求精确一次需引入发件去重，成本高，不建议本期做。
- 发送线程池按 `declare_info_id` 分片串行（同群任务排队，异群并行），避免同群并发写同一记录。

---

## 11. 管理后台 REST API（映射原型交互）

| 原型交互 | 接口 | 说明 |
|----------|------|------|
| 主表格 | `GET /api/declare-infos` | 群配置列表（含邮箱、发送时间、今日消息数） |
| 【新增群】下拉 | `GET /api/wechat-rooms` | **代理**其他组群列表 HTTP 接口，本地缓存 5min；返回时附带 `configured` 标记供前端过滤 |
| 【新增群】保存 | `POST /api/declare-infos` | 后端二次校验 roomId 唯一（含 valid=0 复活逻辑，§4.1） |
| 【修改】 | `PUT /api/declare-infos/{id}` | roomId 不可改（后端忽略该字段变更） |
| 【删除】 | `DELETE /api/declare-infos/{id}` | 逻辑删除 valid=0 |
| 【配置邮件】保存 | `PUT /api/declare-infos/{id}/emails` | TO/CC + 发送时间整体覆盖（事务内先删后插），后端做邮箱格式与服务端去重校验 |
| 【今日消息】 | `GET /api/declare-infos/{id}/messages/today` | 当日消息（发送人/时间/内容） |
| 【发送记录】 | `GET /api/send-records?date=&status=&roomId=` | 分页列表 |
| 【查看详情】 | `GET /api/send-records/{id}` | 记录 + 失败原因 + 明细消息列表 |
| （预留）手动重发 | `POST /api/send-records/{id}/resend` | 原型未含按钮，先留后端接口 |

---

## 12. 配置项清单

```yaml
wecom:
  mail:
    room-list-api-url: http://other-team/api/rooms   # 其他组群列表接口
    room-list-cache-ttl: 5m
    send:
      schedule-cron: "0 * * * * ?"     # 扫描频率(每分钟)
      sending-timeout: 10m             # SENDING 卡死判定阈值
      retry-max-count: 3
      retry-backoff-minutes: [5, 15, 30]
    retention:
      message-days: 90                 # 消息表保留天数(清理Job,待确认)
      record-days: 365                 # 发送记录保留天数(待确认)
spring:
  mail:
    host / username / password / from: 运维提供
    properties.mail.smtp.connectiontimeout: 5000
    properties.mail.smtp.timeout: 10000
    properties.mail.smtp.writetimeout: 10000
```

---

## 13. 关键时序

### 13.1 MQ 消费（正常 + 重复）

```
MQ ──► Consumer: 校验(text/roomid/已配置群)
              │
              ├─ INSERT uk_msg_id 成功 ──commit──► ack
              └─ DuplicateKey ──────────────────► ack(幂等忽略)
```

### 13.2 定时发送（多实例视角）

```
实例A/实例B 每分钟扫描(同一时刻)
   │
   ├─ INSERT IGNORE WAITING (uk_group_date 只成功一次)
   ▼
实例A: CAS WAITING→SENDING 成功 ──► 查未发送消息 ──► 写明细
实例B: CAS affected=0 ──► 放弃
   │
实例A: SMTP 发送 ──成功──► SUCCESS(send_time=now)
   │                └─失败──► FAIL(error_message, retry_count+1)
   │                            └─5/15/30min 后分钟扫描再 CAS 重试
   └─实例A 中途宕机 ──► SENDING 超10min ──► 扫描置 FAIL ──► 重试
```

---

## 14. 待确认决策点（确认后即可开工）

| # | 决策点 | 建议 | 影响 |
|---|--------|------|------|
| D1 | MQ 产品与 Topic/Tag 约定（RocketMQ / RabbitMQ / Kafka） | 倾向 RocketMQ（重试+DLQ 原生） | Consumer 实现、重试语义 |
| D2 | 未配置群的消息：直接丢弃，还是全量入库后查询时过滤 | 建议**丢弃 + 监控计数**（表干净）；若存在"先来消息后配群"场景则需全量入库 | 消息表容量、消费逻辑 |
| D3 | 汇总窗口：严格"自然日当天" vs "上次成功发送以来的所有未送达消息" | 建议**后者**（§8.2），严格自然日会永久丢失 18:00 之后到的消息 | 核心查询 SQL、业务预期 |
| D4 | 当天无消息：不发送且不落记录 vs 落一条空 SUCCESS/SKIPPED 记录 | 建议**不发送、不落记录**（日志+监控即可）；若审计要求"每天都留痕"则加 SKIPPED 状态 | 状态枚举、原型 UI 无需改 |
| D5 | `action` 字段是否存在 `recall`（撤回）等语义需要同步处理 | 需与上游确认；本期仅处理 send | 消费逻辑 |
| D6 | 发送频率是否永远"每日一封" | **已按"每日多轮"实现**：uk(群,日,轮次 round_no)。到点发第 1 轮；当日已 SUCCESS 后又来新消息，距上一轮超过补发延迟(默认 10 分钟,可配 0)自动发补充轮，迟到消息当天必达 | 记录表唯一键 |
| D7 | 终态 FAIL 的告警通道（邮件/IM/仅日志） | 建议至少 ERROR 日志 + 简单邮件通知管理员 | 运维 |
| D8 | SMTP 账号/发件人；消息与记录保留天数、清理策略 | 需运维/业务给数 | 配置项、清理 Job |
| D9 | 群列表 HTTP 接口契约（鉴权方式、返回结构、是否分页） | 拿到接口文档后定代理实现 | Admin API |
| D10 | 是否需要「手动重发」入口（后端已预留 API） | 建议二期加前端按钮 | 原型迭代 |

---

## 15. 实施里程碑（确认 D1~D10 后启动）

| 阶段 | 内容 | 验收标准 |
|------|------|----------|
| M1 | 工程骨架 + DDL 执行 + 实体/Mapper | 表结构落库，代码骨架可启动 |
| M2 | MQ Consumer：校验链 + 幂等入库 | 重复 msgid 只入库一次；非法消息正确忽略 |
| M3 | 定时扫描 + 状态机（CAS、超时兜底、停机补发） | WAITING→SENDING→SUCCESS/FAIL 全链路可观测 |
| M4 | 邮件组装 + SMTP 发送 + 自动重试 | 真实邮箱收到汇总邮件；模拟 SMTP 失败能按退避重试 |
| M5 | Admin REST API + 前端原型联调 | 原型页面对接真实接口走通全部按钮 |
| M6 | 多实例部署 + 异常场景测试（宕机、卡死、重复消费、迟到消息） | 双实例并发无重复邮件；异常场景符合 §9/§10 预期 |
