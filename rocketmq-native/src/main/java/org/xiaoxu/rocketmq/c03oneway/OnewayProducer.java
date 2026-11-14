package org.xiaoxu.rocketmq.c03oneway;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.xiaoxu.rocketmq.MqConst;
import java.nio.charset.StandardCharsets;

/**
 * c03 单向发送（Oneway / Fire-and-Forget）
 *
 * 【知识点】
 * 1. sendOneway()：发出去就不管了——不等 Broker 确认、没有回调、没有返回值。
 *    吞吐最高、可靠性最低，可能丢消息。
 * 2. 典型场景：日志上报、埋点打点、指标采集——丢一条不心疼的数据。
 *    （和同步、异步的取舍：可靠性 oneway < async < sync，吞吐 oneway > async > sync）
 *
 * 【预期输出】10 条瞬间打完（本地测试几乎感觉不到耗时），没有任何回执信息。
 */
public class OnewayProducer {

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(MqConst.GROUP_ASYNC_PRODUCER);
        producer.setNamesrvAddr(MqConst.NAMESRV_ADDR);

        try {
            producer.start();
            System.out.println("生产者启动成功，开始单向发送 10 条...");

            long begin = System.currentTimeMillis();
            for (int i = 1; i <= 10; i++) {
                Message msg = new Message(
                        MqConst.TOPIC_FIRE,
                        "TagB",
                        "oneway-key-" + i,
                        ("单向消息 " + i).getBytes(StandardCharsets.UTF_8));

                // 注意返回值是 void：连 SendResult 都没有，生死自负
                producer.sendOneway(msg);
            }
            long cost = System.currentTimeMillis() - begin;

            System.out.printf("10 条 oneway 消息发送完毕，耗时 %d ms（对比同步发送能明显感知差距）%n", cost);
        } finally {
            producer.shutdown();
        }
    }
}
