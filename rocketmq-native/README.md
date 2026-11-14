# rocketmq-native — RocketMQ 原生客户端学习工程

不用 Spring 注解、不用 starter，纯 `rocketmq-client` 经典 API 从零练起。
每个案例都是**可直接运行的 main 类**，带详细中文注释。

> 依赖：`org.apache.rocketmq:rocketmq-client:5.1.4`（与 three-major-mq-service 中
> starter 2.3.5 内部版本同代；这是市面上资料最多、公司项目最常用的经典 API）。
> 熟练之后再用 `rocketmq-spring-boot-starter` / `rocketmq-client-java`(5.x gRPC) 就是降维打击。

## 环境约定

- NameServer 地址统一在 [MqConst.java](src/main/java/org/xiaoxu/rocketmq/MqConst.java) 里改，当前指向虚拟机 `192.168.200.128:9876`。
- 跑之前确认 NameServer + Broker 端口通：`Test-NetConnection 192.168.200.128 -Port 9876`（Broker 还要看 10911）。
- IDEA 里直接右键类 -> Run。所有消费者案例都会在收满指定条数后自动退出，不用手动停。

## 学习路线（基础篇 01-07）

按编号顺序练，每个案例注释里都有【运行顺序】【知识点】【预期输出】。

| # | 案例 | 核心类 | 一句话知识点 |
|---|------|--------|--------------|
| 01 | 同步发送 + 简单消费 | `c01sync.SyncProducer` / `SimpleConsumer` | 生产/消费三步曲；SendResult；Push 消费=长轮询；消费位点；CONSUME_SUCCESS / RECONSUME_LATER |
| 02 | 异步发送 | `c02async.AsyncProducer` | SendCallback；为什么必须等回调落地再 shutdown |
| 03 | 单向发送 | `c03oneway.OnewayProducer` | sendOneway；可靠性 vs 吞吐的取舍 |
| 04 | 延迟消息 | `c04delay.DelayProducer` / `DelayConsumer` | 18 个延迟级别；SCHEDULE_TOPIC 藏消息原理；bornTimestamp 对账实际延迟 |
| 05 | 顺序消息 | `c05order.OrderProducer` / `OrderConsumer` | MessageQueueSelector 分片键选队列；MessageListenerOrderly 队列内串行；局部有序 vs 全局有序 |
| 06 | 广播模式 | `c06broadcast.BroadcastDemo` | CLUSTERING vs BROADCASTING；广播无重试；instanceName |
| 07 | Tag 过滤 | `c07tag.TagFilterDemo` | subscribe 第二参数表达式；服务端(Broker)过滤原理 |

推荐练法：先跑通 -> 读注释 -> **合上代码自己再写一遍** -> 故意改坏（删掉 selector、把 orderly 换成 concurrently）观察现象。

## 基础概念速查

```
Producer ──┐                                   ┌──> Consumer (Group A 实例1)
           │   ┌──────────┐    ┌─────────┐     │        └ 实例2   ─┐
Sync/Async ├──>│NameServer│───>| Broker  │─────┴──> Consumer (Group B)   │ 负载均衡: 同组分摊
/Oneway    │   │ (路由中心) │    │Topic->Queue│  └ 广播: 每实例全收
           │   └──────────┘    └─────────┘
```

| 概念 | 白话解释 |
|------|---------|
| NameServer | 无状态路由中心：只告诉客户端"Topic 在哪个 Broker"，不碰消息 |
| Broker | 真正存消息、投递消息的服务 |
| Topic | 一级消息分类（发/收的"地址"） |
| Tag | Topic 下的二级标签，Broker 端过滤用 |
| MessageQueue | Topic 的物理分片（默认 4 个），顺序和负载均衡的单位 |
| Producer Group | 生产者逻辑分组，事务消息回查时用到 |
| Consumer Group | 消费者逻辑分组：同组分摊、异组全收；位点/重试/死信都挂在组上 |
| 消费位点(offset) | 组内消费到哪了；集群模式存 Broker，广播模式存本地 |

## 三种发送方式对比

| 方式 | API | 返回 | 可靠性 | 典型场景 |
|------|-----|------|--------|---------|
| 同步 | `send(msg)` | SendResult，阻塞等确认 | 最高 | 订单、支付 |
| 异步 | `send(msg, callback)` | 立即返回，回调通知 | 高 | RT 敏感的通知 |
| 单向 | `sendOneway(msg)` | void，发完就忘 | 最低 | 日志、埋点 |

## 常见坑速查（初学必看）

1. **`sendDefaultImpl call timeout` / `No route info of this topic`**
   NameServer 没连上，或 Broker 没注册到 NameServer。用 `mqadmin clusterList -n 127.0.0.1:9876` 检查。
2. **`connect to <内网IP>:10911 failed`（发送超时但能拿到路由）**
   经典坑：Broker 把自己的内网/错误 IP 注册给了 NameServer。`broker.conf` 里设
   `brokerIP1=127.0.0.1` 后重启 broker。Docker 部署时尤其常见。
3. **异步发送丢回调**：主线程 send 完立刻 `shutdown()`，回调线程还没执行。
   必须等回调全部落地（见 `AsyncProducer` 的 CountDownLatch）。
4. **消费者跑了一下就退出**：`start()` 非阻塞，main 结束进程就没了。
   学习工程用 latch 挂住；真实项目靠容器常驻。
5. **topic 不存在**：默认 broker `autoCreateTopicEnable=true` 时发消息会自动建；
   生产环境通常关掉，需要 `mqadmin updateTopic -n 127.0.0.1:9876 -b 127.0.0.1:10911 -t TopicX` 手动建。
6. **改了 ConsumeFromWhere 不生效**：它只在消费者组**第一次**上线时生效，
   位点一旦记录（Broker 端 `%RETRY%`/offset 表，本地 `.offset` 文件）就以位点为准。
   学习期想重新从头消费：换个 Group 名最省事。

## 进阶篇预告（练完基础篇再来加）

- 08 批量发送（`send(List<Message>)` 与大小限制）
- 09 消费重试 + 死信队列（RECONSUME_LATER、`%RETRY%`/`%DLQ%`、最大重试次数）
- 10 事务消息（`sendMessageInTransaction`、本地事务、回查）
- 11 SQL92 属性过滤（`MessageSelector.bySql`）
- 12 消息轨迹 / Dashboard（rocketmq-dashboard）排查实战
- 之后切注解版：`rocketmq-spring-boot-starter` 对比原生 API 的映射关系
