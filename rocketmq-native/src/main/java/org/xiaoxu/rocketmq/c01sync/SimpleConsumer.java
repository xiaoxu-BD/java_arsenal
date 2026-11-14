package org.xiaoxu.rocketmq.c01sync;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.xiaoxu.rocketmq.MqConst;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * c01-2 最简单的 Push 消费者
 *
 * 【运行顺序】可先跑本类（挂住等消息），再跑 {@link SyncProducer}；消息会被实时推过来。
 *
 * 【知识点】
 * 1. "Push 消费"的真相：不是 Broker 主动推，而是客户端内部用【长轮询】不断拉，
 *    拉到后回调我们注册的监听器——写起来像 MQ 推给你，底层其实是智能的 pull。
 * 2. 消费者组：同一个组内的多个消费者实例【分摊】队列（负载均衡）；
 *    不同组【互不影响】，各自收到全量消息。
 * 3. ConsumeFromWhere：首次上线（本地无消费位点）时从哪开始消费——
 *    CONSUME_FROM_LAST_OFFSET 从最新开始（默认，只收启动之后的新消息）；
 *    CONSUME_FROM_FIRST_OFFSET 从头开始（会把历史消息全倒出来）。
 *    注意：一旦消费过，位点就存在 Broker（或本机文件），这个配置只在"第一次"生效。
 * 4. 监听器返回值就是"消费结果应答"：
 *    CONSUME_SUCCESS  消费成功，Broker 记录位点
 *    RECONSUME_LATER  消费失败，这条消息过一会重投给我（重试机制，进阶篇细练）
 *
 * 【预期输出】3 行 [收到消息] ... Hello RocketMQ 同步消息 x，然后自动退出。
 */
@Slf4j
public class SimpleConsumer {

    /** 收满 3 条后放行 main 线程，让学习进程自动退出（真实项目是常驻进程） */
    private static final CountDownLatch LATCH = new CountDownLatch(3);

    public static void main(String[] args) throws Exception {
        // 消费者组名必填：负载均衡、消费位点、重试队列、死信队列都挂在组维度上
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MqConst.GROUP_SYNC_CONSUMER);
        consumer.setNamesrvAddr(MqConst.NAMESRV_ADDR);

        // 首次上线从最新开始消费（只收本进程启动之后发的新消息）
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        /*
         * 订阅：topic + tag 表达式。
         * "*" 或 null 表示该 topic 下所有 tag 都要（c07 细练过滤）。
         * 一个消费者可以 subscribe 多个 topic。
         */
        consumer.subscribe(MqConst.TOPIC_SYNC, "*");

        /*
         * 注册监听器。这里故意用匿名内部类而不是 lambda——初学阶段能直观看到：
         * 实现的是 MessageListenerConcurrently（并发监听）接口，
         * 方法名 consumeMessage，参数是消息列表 + 上下文，返回值是消费状态。
         *
         * concurrently（并发消费）：客户端线程池多线程同时回调这个方法，吞吐高，但不保证顺序。
         * msgs 列表默认每批只有 1 条（consumeMessageBatchMaxSize 默认 1）。
         */
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                             ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    // MessageExt 是 Message 的子类，带了一堆 Broker 附加信息：
                    // msgId、重试次数 getReconsumeTimes()、bornTimestamp（消息出生时间）等
                    log.info("[收到消息] msgId= {}, 重试次数= {}, body= {}",
                            msg.getMsgId(),
                            msg.getReconsumeTimes(),
                            new String(msg.getBody(), StandardCharsets.UTF_8));
                    LATCH.countDown();
                }
                // 模拟业务处理成功。实际项目里：return 前完成业务逻辑，失败返回 RECONSUME_LATER
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });


        // start() 是非阻塞的：内部启动长轮询、负载均衡、消费线程池，然后立刻返回
        consumer.start();
        log.info("消费者已启动，等待消息...（收满 3 条自动退出）");

        /*
         * start() 返回后 main 线程若直接跑完，进程就退出了，消费者也就死了。
         * 用 CountDownLatch 挂住 main，收满 3 条自动放行；
         * 最多等 60 秒，避免消息一直不来进程卡死。
         * 真实项目里消费者进程常驻，靠 Spring 容器/守护线程维持生命周期。
         */
        boolean done = LATCH.await(60, TimeUnit.SECONDS);
        log.info(done ? "收满 3 条，消费结束" : "60 秒没等够 3 条，先退了");

        /*
         * 【真实踩坑记录】消费成功 ≠ 位点已提交！
         * 位点提交是客户端后台定时任务【异步、每 5 秒一次】批量上报 Broker 的。
         * 如果收满就立刻 shutdown 退出，broker 上的位点还停留在旧值——
         * dashboard 的 Consumer 页会显示"差值/堆积"不为 0，重启后还会重复消费这批消息
         * （at-least-once 语义）。这里特意等 6 秒让位点上报落地，再退出。
         */
        log.info("等 6 秒让消费位点上报到 broker（否则 dashboard 会显示假堆积）...");
        TimeUnit.SECONDS.sleep(6);

        consumer.shutdown();
    }
}
