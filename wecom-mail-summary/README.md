# 企业微信群消息汇总发送邮件系统

接收其他开发组通过 MQ 推送的企业微信群消息（仅 `msgtype=text` 且 `roomid` 非空的群消息），按每个群独立配置的「收件人 + 抄送人 + 每日固定发送时间」，在每天到点后把该群**尚未成功发送**的消息汇总成一封邮件发出，留痕发送结果、支持失败重试。

> **本版本已移除全部 Mock**：群列表走真实 HTTP 接口、消息走真实接入通道、邮件走真实 SMTP、数据全部落库（默认文件持久化 H2，可切 MySQL）。管理页面由后端直接提供（同源，无 CORS 依赖）。

## 两个外部接入节点（不要混淆）

| 节点 | 方向 | 时机 | 实现 |
|------|------|------|------|
| ① 群列表 | 我们 → 其他组（拉） | 管理员配置 roomId 打开下拉框时 | `WechatRoomClient`（url 未配置=后端写死 Mock，已配置=真实 HTTP）→ 全量镜像落库 `t_e_wechat_room`（TTL 缓存，上游挂了回退本地缓存）。**接口只提供 roomId，群名称由管理员人工确认填写** |
| ② MQ 消息推送 | 其他组 → 我们（推） | 业务方在企业微信发消息之后 | `WechatMessageConsumer.onMessage`：校验链 + `uk_msg_id` 幂等落库 |

- 后端设计文档：[docs/backend-design.md](docs/backend-design.md)
- MySQL 正式 DDL：[docs/schema-mysql.sql](docs/schema-mysql.sql)（另有 `src/main/resources/schema-mysql.sql` 供 Spring 首次自动建表用）
- 联调工具：[test-tools/](test-tools/)（假群列表接口 `rooms.json` + 迷你 SMTP 收件服务器）

## 快速开始

要求 JDK 17（本机 Maven 默认 JDK 8，构建需显式指定 JAVA_HOME）。

```bash
# 构建
JAVA_HOME=<JDK17路径> mvn -DskipTests package

# 运行(默认文件持久化 H2 → ./data/,重启数据不丢)
java -jar target/wecom-mail-summary-1.0.0-SNAPSHOT.jar
```

打开管理页面：`http://127.0.0.1:18099/`（页面与 API 同源，所有数据来自数据库）。

### 必配/可选配置（application.yml 或启动参数）

```bash
# 群列表接口(不配则「新增群」下拉不可用)
--wecom.room-list-api.url=http://其他组地址/api/rooms

# SMTP(不配则到点发送记为 FAIL: "SMTP 未配置")
--spring.mail.host=smtp.company.com --spring.mail.port=465 ...

# 切真实 MySQL(先执行 docs/schema-mysql.sql,或首次启动加自动建表参数)
--spring.profiles.active=mysql
#   首次自动建表(--spring.sql.init.mode=always --spring.sql.init.schema-locations=classpath:schema-mysql.sql)
```

群列表接口响应格式约定（`WechatRoomClient.fetchRooms` 一处可改）：`[{"roomId":"ROOM_001","groupName":"货物申报群A"}]` 或 `{"data":[...]}`。

## 联调 / 自测剧本

```bash
# 起两个联调桩(模拟其他组的群列表接口 + 本地收件 SMTP,可选)
cd test-tools
python -m http.server 18090 --bind 127.0.0.1     # 群列表: http://127.0.0.1:18090/rooms.json
python mini_smtp.py 3025                          # 迷你 SMTP,收到邮件写 received_mails.log
```

### 模拟对方 MQ 推送消息实体（接入节点②）

`test-tools/push_mq.py` 构造与线上 MQ **完全一致的消息实体**，POST 到 `/api/messages`——
该接口与未来真实 MQ Listener 调用同一个 `WechatMessageConsumer.onMessage`，幂等/校验/落库行为与线上推送完全相同：

```bash
cd test-tools
python push_mq.py --room ROOM_001 --count 3              # 随机发送人/内容连推 3 条
python push_mq.py --room ROOM_001 --count 5 --interval 3 # 每 3 秒 1 条,共 5 条
python push_mq.py --room ROOM_002 --from 张三 --content "今天有3票货物需要申报"
python push_mq.py --room ROOM_001 --msgid FIXED-ID --content x   # 固定 msgid 再执行一次 → DUPLICATED(幂等)
python push_mq.py --room ROOM_004 --count 1              # 未配置群 → IGNORED_ROOM_NOT_CONFIGURED
```

返回值含义：`CONSUMED` 已落库｜`DUPLICATED` 重复 msgid 幂等忽略｜`IGNORED_NOT_TEXT` / `IGNORED_NO_ROOMID` / `IGNORED_ROOM_NOT_CONFIGURED` / `IGNORED_INVALID` 按规则忽略。

等价 HTTP 方式（与消息实体原文一致）：

```bash
curl -X POST http://127.0.0.1:18099/api/messages -H 'Content-Type: application/json' -d \
  '{"msgid":"MSG-1","action":"send","from":"zhangsan","roomid":"ROOM_001",
    "msgtime":1730000000000,"msgtype":"text","text":{"content":"hello"}}'
```

推送后在页面「今日消息」弹窗可见；到达该群配置的发送时间（或手动 `POST /api/jobs/scan`）即汇总发送。

## 接口一览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/`（静态页） | 管理后台页面（真实 API 驱动，随应用一起部署） |
| GET | `/api/declare-infos` | 群配置列表（含邮箱、发送时间、今日消息数） |
| GET | `/api/wechat-rooms` | 群列表：仅 roomId（url 未配置=后端 Mock，已配置=调其他组接口）→ 落库缓存 → 返回（附 configured/source） |
| POST | `/api/declare-infos` | 新增群（body 需带 roomId 之外**人工确认的群名称**；roomId 二次去重 + 复活式更新） |
| PUT | `/api/declare-infos/{id}` | 修改群（roomId 不可改） |
| DELETE | `/api/declare-infos/{id}` | 删除群（逻辑删除） |
| PUT | `/api/declare-infos/{id}/emails` | 配置邮件（TO/CC 校验 + 发送时间，整体覆盖） |
| GET | `/api/declare-infos/{id}/messages/today` | 今日消息 |
| GET | `/api/send-records?limit=` | 发送记录列表 |
| GET | `/api/send-records/{id}` | 发送详情（含消息明细） |
| POST | `/api/send-records/{id}/resend` | 手动重发（仅 FAIL） |
| POST | `/api/messages` | MQ 消息接入（联调/补录；MQ 产品确定后由 Listener 回调同一逻辑） |
| POST | `/api/jobs/scan` | 手动触发一次定时扫描 |

## 数据表（6 张）

`t_e_wechat_room`（群列表缓存，仅 roomId） / `t_e_cargo_declare_info`（群配置，群名人工填写） / `t_e_client_email`（TO/CC） / `t_e_enterprise_wechat_record`（消息，uk_msg_id 幂等） / `t_e_wechat_mail_send_record`（发送记录，uk 群+日） / `t_e_wechat_mail_send_detail`（邮件↔消息明细）。

## 关键实现位置

- 持久层：**MyBatis-Plus 简化用法**——Mapper 继承 `BaseMapper` + 注解 SQL（`org.xiaoxu.wecommail.mapper`），无 XML / 无代码生成器 / 无 IService 包装层；create_time/update_time 由 `MybatisMetaObjectHandler` 自动填充
- 两个接入节点：`web/WechatRoomClient`（群列表，Mock/HTTP 双模式）｜`consumer/WechatMessageConsumer`（MQ 消费）
- MQ 幂等：`uk_msg_id` 唯一键 + DuplicateKey 吞并；未配置群丢弃（`IGNORED_ROOM_NOT_CONFIGURED`）
- 未成功发送消息查询：`EnterpriseWechatRecordMapper.findUnsentMessages`（NOT EXISTS 反查 SUCCESS 明细，覆盖迟到消息/失败追补）
- 状态机 CAS：`MailSendRecordMapper.casClaimToSending / markSuccess / markFail / markStuckSendingFailed`
- 每分钟扫描：`MailSendScheduler.scan`（超时兜底 → 到点建任务/迟到消息补充轮 → 认领/退避重试）
- **一天多轮机制**：发送记录 uk(群, 日, 轮次)。到点发第 1 轮；当日已 SUCCESS 后又来新消息，距上一轮超过 `wecom.mail.send.late-resend-delay-minutes`（默认 10 分钟，0=来了就发）自动发**补充轮**，保证迟到消息当天必达；每轮固定 `Message-ID`，同轮失败重试复用同一 ID 便于收件端识别极小概率的重复（at-least-once）
- 注：MyBatis 语句按书写顺序执行，不存在 JPA/Hibernate「先 INSERT 后 DELETE」撞唯一键的刷写顺序问题（此前 JPA 版本的已知坑已随重构消失）

## 待外部输入的配置项

| 项 | 状态 |
|----|------|
| 群列表接口 URL 与正式契约 | 用 `wecom.room-list-api.url` 配置；字段映射集中在 `WechatRoomClient.fetchRooms` |
| MQ 产品（RocketMQ/RabbitMQ/Kafka）与 Topic | 确定后新增 Listener 回调 `WechatMessageConsumer.onMessage`，`POST /api/messages` 保留作联调补录 |
| SMTP 账号 | `spring.mail.*` |
| MySQL 账号 | `mysql` profile（注意远程授权，如 `Access denied for 'root'@'192.168.200.1'` 需在库端开权限或换专用账号） |
