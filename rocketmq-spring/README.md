# rocketmq-spring — RocketMQ 注解版学习工程

`rocketmq-spring-boot-starter 2.3.5` + Spring Boot 3.2。与 [rocketmq-native](../rocketmq-native/README.md) 一一映射：
**每个注解属性/配置项，都对应你原生篇练过的一个 API**——机制完全没变，只是换了层皮。

## 启动

1. IDEA 跑 `RocketMqSpringApplication`
2. 浏览器按需点接口（消费者随应用常驻，不再像原生案例跑完退出）：

| 接口 | 对照原生 | 看什么 |
|------|---------|--------|
| `GET /send/sync` | c01 | 返回 SEND_OK + msgId |
| `GET /send/async` | c02 | 回调在控制台日志（HTTP 响应先返回） |
| `GET /send/oneway` | c03 | 无回执 |
| `GET /send/delay` | c04 | DelayConsumer？——注意本工程暂未配延迟消费者，消息进 topic 但没人听 |
| `GET /send/order?orderId=1001` | c05 | OrderSpringConsumer 日志：5 步有序、线程固定 |
| `GET /send/tag` | c07 | TagFilterSpringConsumer 只收 6/9 条 |
| `GET /send/broadcast` | c06 | 广播效果需跑两个实例体会 |

## 原生 API ↔ starter 映射表（灵魂）

### 生产端

| 原生 | starter |
|------|---------|
| `new DefaultMQProducer(group)` | yml: `rocketmq.producer.group` |
| `setNamesrvAddr(...)` | yml: `rocketmq.name-server` |
| `setSendMsgTimeout(...)` / `setRetryTimesWhenSendFailed(...)` | yml: `producer.send-message-timeout` / `retry-times-when-send-failed` |
| 自己 `start()/shutdown()` | 容器托管：启动即 start，关闭即 shutdown |
| `send(msg)` | `rocketMQTemplate.syncSend(dest, msg)` |
| `send(msg, callback)` | `rocketMQTemplate.asyncSend(dest, msg, callback)` |
| `sendOneway(msg)` | `rocketMQTemplate.sendOneWay(dest, msg)` |
| `send(msg, selector, key, timeout)`（顺序） | `rocketMQTemplate.syncSendOrderly(dest, msg, hashKey)` |
| `msg.setDelayTimeLevel(n)` | `syncSend(dest, msg, timeout, delayLevel)` |
| `new Message(topic, tag, ...)` | destination 语法 **`"topic:tag"`**（冒号拼 tag，starter 特色） |

### 消费端

| 原生 | starter |
|------|---------|
| `new DefaultMQPushConsumer(group)` | `@RocketMQMessageListener(consumerGroup=...)` |
| `subscribe(topic, expr)` | `topic=...` + `selectorExpression=...` |
| `MessageListenerConcurrently` | `consumeMode = CONCURRENTLY`（默认） |
| `MessageListenerOrderly` | `consumeMode = ORDERLY` |
| `setMessageModel(BROADCASTING)` | `messageModel = BROADCASTING` |
| 返回 `CONSUME_SUCCESS` | `onMessage()` 正常返回 |
| 返回 `RECONSUME_LATER` | `onMessage()` **抛异常**（自动进重试，c08 三部曲照旧） |
| 手工 `new String(body, UTF_8)` 解码 | 泛型 `RocketMQListener<String>` / 实体类自动 JSON 反序列化 |

## 你踩过的坑在 starter 里的形态

| 原生篇的坑 | starter 里的对应物 |
|-----------|-------------------|
| start 后 main 退出消费者就死 | 容器常驻，生命周期托管（坑消失） |
| 位点 5 秒异步提交、dashboard 假 diff | 机制照旧（dashboard 照样可能短暂显示 diff） |
| 重复消费 / at-least-once | 照旧——**消费逻辑仍必须幂等** |
| body 编码不一致乱码 | 泛型转换统一序列化，基本消失；跨语言消费仍注意 |
| 同组订阅关系必须一致 | **照旧**！每个 @RocketMQMessageListener 是独立组，别让两个 Listener 用同一个组订不同表达式 |

## 下一步（s03 事务消息）

starter 版最值得练的增量：`@RocketMQTransactionListener` + `sendMessageInTransaction`——
半消息、本地事务、回查，原生篇没练的重头戏，练完 RocketMQ 主线就齐了。
