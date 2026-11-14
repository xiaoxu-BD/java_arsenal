package org.xiaoxu.rocketmq.c06broadcast;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
// 5.x 客户端里 MessageModel 从 common.protocol 挪到了 remoting.protocol 包
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.xiaoxu.rocketmq.MqConst;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * c06 广播模式（BROADCASTING）vs 集群模式（CLUSTERING）
 *
 * 一个 main 演示全流程：起 2 个同组广播消费者 -> 发 5 条 -> 两个消费者【各收 5 条】。
 *
 * 【知识点】
 * 1. MessageModel 两种消息模型（默认 CLUSTERING）：
 *    CLUSTERING 集群模式：同组消费者【分摊】队列，一条消息只被组内一个实例消费。
 *                        位点存在 Broker 上，支持重试、死信 —— 业务主流模式。
 *    BROADCASTING 广播模式：一条消息会推给【组内每个】实例，人人有份。
 *                        典型场景：本地缓存刷新、配置变更广播、每台机器都要执行的指令。
 * 2. 广播模式两个重要差异：
 *    a) 消费位点存在【消费者本地文件】（不是 Broker），实例挂了重启从本地点位继续；
 *    b) 【没有重试/死信】——消费失败这条就丢了（Broker 不为广播模式建重试队列），
 *       所以广播消费逻辑要自己兜底。
 * 3. instanceName：同一台机器/同一 JVM 里的客户端实例标识（clientId 的一部分）。
 *    本例在一个 JVM 里起两个消费者，若不区分 instanceName，两个实例在负载均衡、
 *    位点管理上会互相干扰——"同机多实例必设不同 instanceName"是常见实战细节。
 *
 * 【预期输出】消费者A、消费者B 各打印 5 条 [消费者X]，合计 10 条。
 */
public class BroadcastDemo {

    /** 两个消费者合计要收到 2 x 5 = 10 条 */
    private static final CountDownLatch LATCH = new CountDownLatch(10);

    public static void main(String[] args) throws Exception {
        DefaultMQPushConsumer consumerA = buildConsumer("broadcast_instance_A", "消费者A");
        DefaultMQPushConsumer consumerB = buildConsumer("broadcast_instance_B", "消费者B");

        consumerA.start();
        consumerB.start();
        System.out.println("两个广播消费者已就绪，等 3 秒让订阅稳定后开始发消息...");
        TimeUnit.SECONDS.sleep(3);

        sendFiveMessages();

        LATCH.await(60, TimeUnit.SECONDS);
        System.out.println("广播演示结束：每个消费者都收到了全量 5 条");
        consumerA.shutdown();
        consumerB.shutdown();
    }

    /** 构造一个广播模式的消费者：同一个组、不同 instanceName */
    private static DefaultMQPushConsumer buildConsumer(String instanceName, String label)
            throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MqConst.GROUP_BROADCAST_CONSUMER);
        consumer.setNamesrvAddr(MqConst.NAMESRV_ADDR);

        // 核心设置：广播模式（默认是 MessageModel.CLUSTERING）
        consumer.setMessageModel(MessageModel.BROADCASTING);

        // 同 JVM 多实例必设不同 instanceName，否则相互干扰
        consumer.setInstanceName(instanceName);

        // 广播模式从头消费：这样消费者启动前发的消息也能收到（演示更直观）
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        consumer.subscribe(MqConst.TOPIC_BROADCAST, "*");

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                             ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    System.out.printf("[%-4s] %s%n", label, new String(msg.getBody(), StandardCharsets.UTF_8));
                    LATCH.countDown();
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        return consumer;
    }

    private static void sendFiveMessages() throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(MqConst.GROUP_BROADCAST_PRODUCER);
        producer.setNamesrvAddr(MqConst.NAMESRV_ADDR);
        producer.start();
        try {
            for (int i = 1; i <= 5; i++) {
                Message msg = new Message(
                        MqConst.TOPIC_BROADCAST,
                        "TagBC",
                        "broadcast-key-" + i,
                        ("广播消息 " + i).getBytes(StandardCharsets.UTF_8));
                producer.send(msg);
            }
            System.out.println("生产者已发出 5 条广播消息");
        } finally {
            producer.shutdown();
        }
    }
}
