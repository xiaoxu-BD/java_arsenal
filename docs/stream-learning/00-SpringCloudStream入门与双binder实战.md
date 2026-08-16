# 00 · Spring Cloud Stream 入门与双 binder 实战（RocketMQ + RabbitMQ）

> 2026-08-16 · 模块 `stream-three-mq` · 所有结论以当日实测为准。
> 前置知识：RocketMQ 原生全链路（见 `docs/rocketmq-learning/05` 复盘页）。

---

## 一、它到底是什么

一句话：**消息中间件界的 JDBC**。

| JDBC 世界 | Stream 世界 |
|---|---|
| `java.sql.Connection` | Binder（绑定器） |
| MySQL / Oracle 驱动 | rocketmq / rabbit / kafka binder |
| SQL（各方言） | `Supplier` / `Function` / `Consumer`（函数式 bean） |
| 换数据库 = 换驱动 + URL | 换 MQ = 换 binder 依赖 + 配置 |

注意它的真名是 **Spring Cloud Stream**（`spring-cloud-stream`，Spring Cloud 家族），
网上搜"springboot stream starter"基本搜不到东西。

抽象层级：你的函数 ↔ **绑定(Binding)** ↔ Binder ↔ 真实 MQ。
业务代码只认识"输入绑定/输出绑定"，中间的差异被 binder 吃掉。

## 二、编程模型（SCS 4.x，Boot 3.x 唯一写法）

老的 `@EnableBinding` + `@StreamListener` 在 4.x 已删除，只剩函数式三件套：

```java
@Bean public Consumer<Message<String>> order() {...}      // 消费 → 绑定名 order-in-0
@Bean public Function<String, String> notifyProcess() {...} // 加工 → notifyProcess-in-0 / -out-0
@Bean public Supplier<String> tick() {...}                 // 定时产 → tick-out-0（本项目未用）
```

**绑定命名规则：`bean名 + "-in-0" / "-out-0"`**（数字留给多输入输出，如聚合函数）。

命令式发送用 `StreamBridge`（不需要 Supplier，适合 HTTP 触发）：

```java
streamBridge.send("order-out-0", MessageBuilder.withPayload(body)
        .setHeader("bizKey", "...").build());   // 自定义 header 全程透传
```

### ⚠️ 第一坑（当天踩）：多函数必须显式声明

多个函数 bean 并存时，必须在 yml 写：

```yaml
spring.cloud.function.definition: order;mail;notifyProcess
```

不写的后果**静默且迷惑**：应用正常启动、StreamBridge 发送正常（消息进 broker），
但**没有任何消费者被创建**——消息在 topic 里堆积等你。日志里只有一条 WARN：
`Multiple functional beans were found [mail, notifyProcess, order], thus can't determine default function definition`。
单函数应用才允许省略此配置。

## 三、双 binder 配置（本模块 yml 逐段解读）

```yaml
spring.cloud.stream:
  binders:                    # ① binder 注册处：一 MQ 一 binder
    rocketmq:
      type: rocketmq          #    type 决定用哪个 binder 实现（SCA 提供）
      environment:            #    该 binder 专属的环境（连接信息互相隔离）
        spring.cloud.stream.rocketmq.binder.name-server: 192.168.200.128:9876
    rabbit:
      type: rabbit
      environment:
        spring.rabbitmq: { host: 192.168.200.128, port: 5672, ... }
  bindings:                   # ② 绑定：bean 名 → 目的地 → 哪个 binder
    order-out-0: { destination: stream-order-topic, binder: rocketmq }
    order-in-0:  { destination: stream-order-topic, group: stream-order-group, binder: rocketmq }
    notifyProcess-in-0:  { destination: stream-notify-topic, group: stream-notify-group, binder: rocketmq }
    notifyProcess-out-0: { destination: stream-notify-result, binder: rabbit }
```

**这就是"换 MQ 只改配置"的全部**：同一个 `order` 函数，把 `binder: rocketmq`
改成 `binder: rabbit`，它就从消费 RocketMQ 变成消费 RabbitMQ，代码零改动。

跨 binder Function 是这个抽象的极致展示：`notifyProcess` 从 RocketMQ 收、往 RabbitMQ 发，
函数本身完全不知道自己横跨了两家 MQ。

## 四、SCS 概念 ↔ 原生概念映射表

| SCS | RocketMQ 语境 | RabbitMQ 语境 |
|---|---|---|
| destination | Topic（如 stream-order-topic） | Exchange（如 stream-mail） |
| group | consumerGroup（集群模式负载均衡） | 队列后缀 `stream-mail.stream-mail-group`，自动声明 exchange+queue+binding |
| binder | rocketmq-client + SCA 封装 | spring-amqp 封装 |
| contentType 协商 | body 序列化（实测默认 application/json） | 同左 |
| 自定义 header | 消息属性透传 | AMQP headers 透传 |

RabbitMQ 侧的拓扑**全自动声明**——对比 `rabbitmq-service` 模块里手写的
`RabbitMQConfig`（Queue/Exchange/Binding bean 一大段），这就是 binder 替你干掉的活。

## 五、当天实测记录

### 成功链路（RocketMQ 侧 + 跨 binder）

```
GET /stream/order → 发送 true → order-in-0 消费：
[RocketMQ 消费] body=rocketmq-msg-1 headers={ROCKET_MQ_MESSAGE_ID=2408820C…,
  ROCKET_MQ_BORN_TIMESTAMP=…, ROCKET_MQ_QUEUE_ID=0, bizKey=ORDER-…, contentType=application/json}

GET /stream/notify → notifyProcess 触发：
[跨binder处理] 收自 RocketMQ: cross-msg-1 → 将发往 RabbitMQ: [processed@…] cross-msg-1
```

binder 会把原生消息属性**翻译成 headers 前缀注入**（`ROCKET_MQ_*`）——
用 `Message<String>` 而非裸 `String` 接收就能看到，排障时这些 header 就是原生世界的望远镜。

### 实测行为三则（文献打架的，以实测为准）

1. **新消费组会补消费历史**：函数定义修复前发送的 2 条"无主"消息，消费者上线后全部捞起
   （从队列头开始）。部分资料称新组默认从最新开始——**至少 SCA 2023.0.1.2 + client 5.1.4
   组合下实测是从头消费**。生产含义：新组上线 = 会重放 topic 里的存量消息，必要时显式控制起点。
2. **RabbitMQ 宕机时应用照常启动**：rabbit 侧消费者后台静默重连（每几秒一次 Connection refused），
   RocketMQ 侧完全不受影响——binder 之间天然隔离。服务恢复后自动接入，无需重启。
   但宕机期间**发往 rabbit 的消息发送即失败**（HTTP 500 / Function 出站失败进 errorChannel），
   Stream 不会替你本地缓存——可靠性仍要靠 broker 侧或重试策略。
3. Dashboard 对账：`stream-order-topic` 3 条 / `stream-notify-topic` 1 条，与消费日志一一对应。

### 踩坑清单（Boot 3.2 + 根 pom 环境）

| 坑 | 现象 | 修法 |
|---|---|---|
| 多函数未声明 definition | 启动正常但零消费者，消息堆积 | `spring.cloud.function.definition: order;mail;notifyProcess` |
| `@RequestParam` 无显式名 | Boot3.2 移除参数名反射兜底 → 500 | 注解写 `name = "body"` |
| 改 yml 未重编译 | `java -cp target/classes` 跑的还是旧配置 | 改资源必须重新 `mvn compile` |

## 六、语义差异警示（Stream 不是完全无感切换）

| 维度 | RocketMQ 原生 | 经 Stream |
|---|---|---|
| 消费重试 | broker 端 %RETRY% 退避 16 次 → %DLQ% | SCS 有应用内 `max-attempts`（默认3）先行；binder 再叠加 broker 行为，**两层重试语义要分开理解** |
| 死信 | %DLQ%+组名，自动 | Rabbit binder 需配置 DLX 才有等价物；SCS 也支持 errorChannel 兜底 |
| 顺序/事务/延迟消息 | 原生 API 丰富 | **binder 只覆盖公共子集**，私有特性要用 binder 专属扩展配置（如 SCA 的 `spring.cloud.stream.rocketmq.bindings.*`） |

"屏蔽差异"护住的是**收发模型**；各家的高级特性（tags/keys、事务、延迟级别）仍会从
binder 扩展配置的缝隙里渗出来——这是抽象的边界，不是缺陷。

## 七、补充实测（2026-08-16 下午收网）

### 双 binder 全链路闭环 ✓

RabbitMQ 凭证修复（yml 改为 admin/123456）后，无需改一行代码：

- `/stream/mail` → `[RabbitMQ 消费]`，headers 里 `amqp_consumerQueue=stream-mail.stream-mail-group`
  证明 **binder 自动声明了 exchange + queue + binding**（对照 rabbitmq-service 手写的 RabbitMQConfig）
- management API(15672) 对账：exchanges `stream-mail` / `stream-notify-result` 自动出现，
  队列 `stream-mail.stream-mail-group` ack 计数增长、积压 0

### 跨 binder 失败的连锁反应（本日最大教训）

cross-msg-1 实录：函数从 RocketMQ 收到消息 → 往 RabbitMQ 出站撞认证墙 → 函数抛异常
→ **RocketMQ 把输入消息判为消费失败**，进 `%RETRY%stream-notify-group` 重试 9 跳、跨约 40 分钟。
凭证修复后第 10 跳成功转发，循环熄火，**无死信**——重试机制扛住了这次"基础设施配置故障"。

三个推论：
1. **下游 MQ 的病，上游 MQ 的重试队列吃药**——跨 binder 架构要把出站失败的兜底设计好；
2. **重试副本计入 broker 生产计数**（修正前文）：`%RETRY%` 每跳写回都是一次 put，
   邮件事故 18 副本 + 本次 9 副本都进了"今日生产"；
3. **broker 的"今天"按 UTC 日切**（= 北京时间早 8 点）：dashboard 上 `11:35:45Z` 即 19:35 CST。
   早上看到的"今日生产 48"实际包含昨天早 8 点以来的全部写入。生产环境做容量日报要校准时区。

### StreamBridge 的进程内短路陷阱（隐蔽，实测踩中）

`streamBridge.send("notifyProcess-in-0", body)`——**发送目标是函数的输入绑定名时，消息不经过 broker**：
StreamBridge 直接把消息塞进函数的输入通道，函数在**HTTP 请求线程**里同步执行（日志线程名 `io-18083-exec-2` 是铁证）。
表现为"2ms 消费成功"，RocketMQ topic 位点纹丝不动——跨 binder 演示实际被砍成了进程内调用。

正确姿势：给发送方**独立的输出绑定**（`notify-out-0` → 同一 destination），StreamBridge 发它才走真 broker。
另一个配对的坑：函数输出指向的 exchange（`stream-notify-result`）**没有带 group 的消费者时不会声明队列**，
消息到 exchange 即被静默丢弃——exchange 详情页只有 publish 速率闪一下。修复 = 加 `notifyResult-in-0`（带 group），
Rabbit 自动建队列 `stream-notify-result.stream-notify-result-group`，结果消息从此可见可查。

## 八、待办

- [x] RabbitMQ 收发 + 跨 binder 出站（2026-08-16 验证通过）
- [x] management API 对账（exchange/queue 自动声明、ack 计数）
- [ ] 失败注入：给 `order` 消费者加 poison 分支，实测 SCS `max-attempts` 与 broker 重试的叠加行为
- [ ] （远期）Kafka binder 加入，验证"三 MQ 一套代码"
