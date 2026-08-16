# 新模块 stream-three-mq：Spring Cloud Stream 双 binder 实战（RocketMQ + RabbitMQ）

> 命名先纠正：这个技术叫 **Spring Cloud Stream**（`spring-cloud-stream`，Spring Cloud 家族成员），不是 springboot-stream-starter。它是"消息中间件 JDBC"——业务代码只写函数，binder 决定消息走哪家 MQ。

## 版本策略（零配置继承）

新模块挂在根 pom（org.xiaoxu:java_arsenal:1.0）下，版本全由根 BOM 托管：
- Spring Boot 3.2.2 / Spring Cloud 2023.0.0 / SCA 2023.0.1.2（均已 import）
- `spring-cloud-starter-stream-rocketmq`（SCA BOM 托管，内核 rocketmq-client 5.1.4，兼容你的 5.x broker）
- `spring-cloud-starter-stream-rabbit`（SC BOM 托管 → SCS 4.1.x）
- 根 pom `<modules>` 追加 `stream-three-mq`（参照 pay 模块先例）

## 目录结构

```
stream-three-mq/
├── pom.xml                    # 继承根 pom，两个 binder starter 均 versionless
├── README.md                  # 架构图、运行指南、端点说明
└── src/main/
    ├── java/org/xiaoxu/
    │   ├── StreamThreeMqApplication.java
    │   ├── controller/DemoController.java   # REST 触发发送（StreamBridge）
    │   └── function/StreamFunctions.java    # 函数式 bean：2 个 Consumer + 1 个 Function
    └── resources/application.yml            # 双 binder 配置（教学核心）
```

## 函数式编程模型（SCS 4.x，无 @EnableBinding）

| bean | 类型 | binder | 演示点 |
|---|---|---|---|
| `order` | `Consumer<Message<String>>` | rocketmq | 绑定名 `order-in-0`，打印 body+headers（看 binder 注入了什么） |
| `mail` | `Consumer<Message<String>>` | rabbit | 同样的代码风格，消费 RabbitMQ（topic exchange + group 队列） |
| `notifyProcess` | `Function<String,String>` | **rocketmq→rabbit 跨 binder** | 一条消息从 RocketMQ 进、处理后从 RabbitMQ 出——屏蔽差异的极致展示 |
| （发送） | `StreamBridge.send(...)` | 控制器里命令式发送 | `order-out-0` / `mail-out-0` 两个输出绑定 |

REST 端点（端口 18083）：
- `GET /stream/order?body=...` → RocketMQ
- `GET /stream/mail?body=...` → RabbitMQ
- `GET /stream/notify?body=...` → RocketMQ 进 → Function 处理 → RabbitMQ 出（一箭双雕）

## application.yml 核心结构（教学重点：差异全部被压进配置）

```yaml
spring.cloud.stream:
  binders:
    rocketmq: { type: rocketmq, environment: { spring.cloud.stream.rocketmq.binder.name-server: 192.168.200.128:9876 } }
    rabbit:   { type: rabbit,   environment: { spring.rabbitmq: { host: 192.168.200.128, port: 5672, username: admin, password: tingqq7zZ } } }
  bindings:
    order-out-0: { destination: stream-order-topic, binder: rocketmq }
    order-in-0:  { destination: stream-order-topic, group: stream-order-group, binder: rocketmq }
    mail-out-0:  { destination: stream-mail, binder: rabbit }
    mail-in-0:   { destination: stream-mail, group: stream-mail-group, binder: rabbit }
    notifyProcess-in-0:  { destination: stream-notify-topic, group: stream-notify-group, binder: rocketmq }
    notifyProcess-out-0: { destination: stream-notify-result, binder: rabbit }
```
RabbitMQ binder 会自动声明 topic exchange + group 队列 + 绑定（destination=exchange 名，group=队列后缀）——正好和你 rabbitmq-service 里手动 `RabbitMQConfig` 声明的拓扑对照讲解。

## 文档（沿用 docs/rocketmq-learning 编号惯例）

`docs/stream-learning/00-SpringCloudStream-入门与双binder实战.md`：
- 编程模型三件套（Supplier/Function/Consumer + StreamBridge）、绑定命名规则（bean 名 + -in/-out-0）
- binder 抽象与"换 MQ 只改配置"的边界（各 binder 私有扩展仍会渗出，如 RocketMQ 的 tags/keys）
- 与前两周知识的映射表：SCS 概念 ↔ 你实测过的 RocketMQ 原生概念（destination↔topic、group↔consumerGroup、DLQ/retry 语义差异）

## 验证方案（照旧实证风格）

1. JDK17 编译、启动，看日志确认双 binder 连接成功
2. curl 三个端点，观察消费者日志（两个 Consumer + Function 的输出）
3. 外部取证：RocketMQ dashboard API 查 `stream-*` topic 消息；RabbitMQ management API（15672）查队列/交换机自动声明情况
4. 失败注入（如时间允许）：向 order 发 poison，观察 SCS 的 `max-attempts`（应用内重试）与 broker 重试的语义差异——衔接已学的重试/死信知识

## 风险与预案

- SCA binder 的函数式绑定个别属性名与文档可能有出入（4.x 较新）：以运行时实测为准，跑通过程本身就是教学素材
- 若跨 binder Function 出现兼容问题，降级为同 binder processor，文档记录原因