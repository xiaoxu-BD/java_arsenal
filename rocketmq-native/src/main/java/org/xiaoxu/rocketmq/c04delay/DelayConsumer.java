package org.xiaoxu.rocketmq.c04delay;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.xiaoxu.rocketmq.MqConst;
import java.nio.charset.StandardCharsets;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * c04-2 延迟消息消费者
 *
 * 【知识点】
 * 1. 消费端代码毫无特殊之处——延迟对消费者透明，到点前根本拉不到这条消息。
 * 2. 对比时间：msg.getBornTimestamp() 是"生产者发出时刻"，
 *    消费时刻减它 = 实际延迟时长，可以和 setDelayTimeLevel 的理论值对账。
 *
 * 【预期输出】3 条消息分别延迟约 1s / 10s / 30s 到达，各打印一行"实际延迟 xx 秒"。
 */
public class DelayConsumer {

    private static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final CountDownLatch LATCH = new CountDownLatch(3);

    public static void main(String[] args) throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MqConst.GROUP_DELAY_CONSUMER);
        consumer.setNamesrvAddr(MqConst.NAMESRV_ADDR);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe(MqConst.TOPIC_DELAY, "*");

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                             ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    long born = msg.getBornTimestamp();                       // 生产者发出时刻
                    long realDelayMs = System.currentTimeMillis() - born;     // 实际延迟
                    String bornTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(born),
                            ZoneId.systemDefault()).format(HH_MM_SS);
                    System.out.printf("[%s] 收到 %s，发出于 %s，实际延迟 %.1f 秒%n",
                            LocalTime.now().format(HH_MM_SS),
                            new String(msg.getBody(), StandardCharsets.UTF_8),
                            bornTime,
                            realDelayMs / 1000.0);
                    LATCH.countDown();
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
        System.out.println("延迟消费者已启动，先跑 DelayProducer，最多等 90 秒...");
        LATCH.await(90, TimeUnit.SECONDS);
        consumer.shutdown();
    }
}
