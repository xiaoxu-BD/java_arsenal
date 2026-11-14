package org.xiaoxu.rocketmq.c05order;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.xiaoxu.rocketmq.MqConst;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * c05-2 顺序消息——消费者：队列内串行消费
 *
 * 【知识点】
 * 1. MessageListenerOrderly（顺序监听）vs c01 的 MessageListenerConcurrently（并发监听）：
 *    - concurrently：消费线程池并行处理不同消息，可能乱序；
 *    - orderly：客户端对队列【加分布式锁】，一个队列同一时刻只被一个消费者、
 *      一个线程消费，消费完一条提交位点再拿下一条——队列内严格串行。
 * 2. 返回值语义也不同：
 *      ConsumeOrderlyStatus.SUCCESS              成功，继续下一条
 *      SUSPEND_CURRENT_QUEUE_A_MOMENT            失败，把【当前队列】挂起片刻后重试本条
 *      （concurrently 是 RECONSUME_LATER 重新入队延迟重投；orderly 是原地挂起重试，
 *        顺序消息不能重新入队——重投可能插到别人后面，顺序就破了）
 * 3. 代价：串行消费吞吐低；某条消息卡死会阻塞整个队列（后面全堵着），
 *    所以顺序消费的业务逻辑要设好超时、避免毒丸消息。
 *
 * 【预期输出】多跑几次会发现：同一订单的 5 个步骤打印顺序永远是 1->2->3->4->5；
 *            不同订单之间可能交错（它们在不同队列、不同线程）。
 */
public class OrderConsumer {

    private static final CountDownLatch LATCH = new CountDownLatch(15);

    public static void main(String[] args) throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MqConst.GROUP_ORDER_CONSUMER);
        consumer.setNamesrvAddr(MqConst.NAMESRV_ADDR);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe(MqConst.TOPIC_ORDER, "*");

        consumer.registerMessageListener(new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs,
                                                        ConsumeOrderlyContext context) {
                for (MessageExt msg : msgs) {
                    // 当前线程名能看到规律：ConsumeMessageThread_xxx，
                    // 同一队列的消息始终由同一线程串行处理
                    System.out.printf("[%s] %s%n",
                            Thread.currentThread().getName(), new String(msg.getBody(), StandardCharsets.UTF_8));
                    LATCH.countDown();
                }
                return ConsumeOrderlyStatus.SUCCESS;
                // 失败时返回 SUSPEND_CURRENT_QUEUE_A_MOMENT，注意会阻塞当前队列
            }
        });

        consumer.start();
        System.out.println("顺序消费者已启动，先跑 OrderProducer...（收满 15 条退出）");

        LATCH.await(60, TimeUnit.SECONDS);
        consumer.shutdown();
    }
}
