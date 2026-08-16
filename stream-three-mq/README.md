# stream-three-mq

用 **Spring Cloud Stream** 一套函数式代码同时驱动 **RocketMQ + RabbitMQ** 的实战模块。
Kafka 位置已预留（将来只需加一个 binder 依赖 + 若干配置行）。

## 架构

```
                        ┌──────────────────────────────────────┐
    HTTP :18083         │        stream-three-mq 应用           │
   ┌──────────────┐     │                                      │
   │ /stream/order├────►│ DemoController ── StreamBridge ──┐   │
   │ /stream/mail │     │                                   │   │
   │ /stream/notify     │ StreamFunctions（纯函数，无 MQ API）  │   │
   └──────────────┘     │                                      │   │
                        │   order: Consumer ◄──── rocketmq binder ◄── stream-order-topic (RocketMQ)
                        │   mail:  Consumer ◄──── rabbit binder  ◄── stream-mail exchange (RabbitMQ)
                        │   notifyProcess: Function（跨 binder）◄── stream-notify-topic (RocketMQ)
                        │        └──────────► rabbit binder ─────► stream-notify-result (RabbitMQ)
                        └──────────────────────────────────────┘
```

业务代码里**没有一行** RocketMQ/RabbitMQ 的 API——消息走哪家 MQ 完全由
`application.yml` 的 `bindings.*.binder` 决定。

## 端点

| 端点 | 链路 | 说明 |
|---|---|---|
| `GET /stream/order?body=xxx` | → RocketMQ `stream-order-topic` | `order` 消费者打印 body + headers |
| `GET /stream/mail?body=xxx` | → RabbitMQ `stream-mail` exchange | `mail` 消费者打印 body + headers |
| `GET /stream/notify?body=xxx` | RocketMQ → **notifyProcess 函数** → RabbitMQ | 跨 binder 加工转发，一箭双雕 |

## 运行

```bash
# 编译（根 pom 要求 JDK17；若默认 JDK 是 8，需覆盖 JAVA_HOME）
JAVA_HOME=<jdk17路径> mvn -f stream-three-mq/pom.xml compile

# 启动（或直接在 IDE 里跑 StreamThreeMqApplication）
java -cp stream-three-mq/target/classes;$(cat stream-three-mq/target/cp.txt) org.xiaoxu.StreamThreeMqApplication
```

前置依赖：`192.168.200.128` 上 RocketMQ（9876）与 RabbitMQ（5672）可达。
RabbitMQ 不可用时应用照常启动，rabbit 侧消费者持续重连，服务恢复后**无需重启**自动接入。

## 版本

继承根 pom BOM，本模块零版本声明：

| 组件 | 版本 | 来源 |
|---|---|---|
| Spring Boot | 3.2.2 | 根 BOM |
| Spring Cloud | 2023.0.0 | 根 BOM |
| Spring Cloud Alibaba | 2023.0.1.2 | 根 BOM |
| rocketmq-client（binder 内核） | 5.1.4 | SCA starter 传递依赖 |

## 已实测（2026-08-16）

- ✅ RocketMQ 收发：`stream-order-topic` 3 条消息全部消费，headers 里可见 binder 注入的
  `ROCKET_MQ_MESSAGE_ID / BORN_TIMESTAMP / QUEUE_ID` 与自定义透传 header
- ✅ RabbitMQ 收发：exchange `stream-mail` + 队列 `stream-mail.stream-mail-group` 由 binder 自动声明
  （management API 对账），`amqp_consumerQueue` 等 headers 可见；凭证 `admin/123456`
- ✅ 跨 binder Function：`stream-notify-topic`(RocketMQ) → `notifyProcess` 加工 → `stream-notify-result`(RabbitMQ)
- ✅ 消费者上线补消费：函数定义修复后，此前"无主"的历史消息被新消费组全部捞起
- ✅ 跨 binder 失败语义实录：RabbitMQ 宕机/认证失败期间，函数出站失败会污染 RocketMQ 侧重试队列
  （cross-msg-1 重试 9 跳后随凭证修复自然熄火，无死信）——详见 docs/stream-learning/00

## 踩坑记录（重要，全部实测）

1. **多个函数 bean 必须显式声明** `spring.cloud.function.definition: order;mail;notifyProcess`，
   否则 SCS 一个消费者都不创建（日志有 WARN 但启动"正常"，极具迷惑性）；
2. Boot 3.2 下 `@RequestParam` 必须写显式参数名（根 pom 编译未开 `-parameters`）；
3. 改 `src/main/resources` 后必须重新 `mvn compile`，`java -cp target/classes` 不会自动感知资源变化。
