package org.xiaoxu.rocketmq;

/**
 * 全局常量：所有案例共用的地址、Topic、Group 都集中在这里，改一处全局生效。
 *
 * ── 概念速查 ──────────────────────────────────────────────────────────────
 * NameServer : 路由中心（类似微服务里的注册中心）。生产者/消费者先问它
 *              "Topic Xxx 在哪几台 Broker 上"，它不存消息、无状态、可集群。
 * Broker     : 真正存消息、转发消息的服务器，Topic 的数据落在它的队列里。
 * Topic      : 一级消息分类，比如"订单Topic"，生产者和消费者靠它对上暗号。
 * Tag        : Topic 下的二级分类/子标签，用于消费端在服务端过滤消息。
 * MessageQueue: Topic 的物理分片，一个 Topic 默认 4 个（readQueueNums=writeQueueNums=4），
 *              是"顺序"和"并发"的最小单位（c05 顺序消息会深刻体会）。
 * Producer Group: 生产者组。普通消息里作用不大，事务消息回查时靠它定位生产者（进阶篇）。
 * Consumer Group: 消费者组。同组消费者分摊消息（负载均衡）；
 *              不同组各自都能收到全量消息（c06 演示）。
 */
public final class MqConst {

    private MqConst() {
    }

    /** NameServer 地址。本地默认 9876 端口；改环境时只需改这里 */
    public static final String NAMESRV_ADDR = "192.168.200.128:9876";

    // ── c01 同步发送 ─────────────────────────────────────────────────────
    public static final String TOPIC_SYNC = "BasicTopic_Sync";
    public static final String GROUP_SYNC_PRODUCER = "basic-sync-producer-group";
    public static final String GROUP_SYNC_CONSUMER = "basic-sync-consumer-group";

    // ── c02 异步发送 / c03 单向发送（复用同一个 Topic） ──────────────────
    public static final String TOPIC_FIRE = "BasicTopic_FireAndForget";
    public static final String GROUP_ASYNC_PRODUCER = "basic-async-producer-group";

    // ── c04 延迟消息 ─────────────────────────────────────────────────────
    public static final String TOPIC_DELAY = "BasicTopic_Delay";
    public static final String GROUP_DELAY_PRODUCER = "basic-delay-producer-group";
    public static final String GROUP_DELAY_CONSUMER = "basic-delay-consumer-group";

    // ── c05 顺序消息 ─────────────────────────────────────────────────────
    public static final String TOPIC_ORDER = "BasicTopic_Order";
    public static final String GROUP_ORDER_PRODUCER = "basic-order-producer-group";
    public static final String GROUP_ORDER_CONSUMER = "basic-order-consumer-group";

    // ── c06 广播模式 ─────────────────────────────────────────────────────
    public static final String TOPIC_BROADCAST = "BasicTopic_Broadcast";
    public static final String GROUP_BROADCAST_CONSUMER = "basic-broadcast-consumer-group";
    public static final String GROUP_BROADCAST_PRODUCER = "basic-broadcast-producer-group";

    // ── c07 Tag 过滤 ─────────────────────────────────────────────────────
    public static final String TOPIC_TAG = "BasicTopic_Tag";
    public static final String GROUP_TAG_PRODUCER = "basic-tag-producer-group";
    public static final String GROUP_TAG_CONSUMER = "basic-tag-consumer-group";
}
