package org.xiaoxu.rocketmq.c08retry;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.xiaoxu.rocketmq.MqConst;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * c08 消费重试 + 死信队列（DLQ）—— 消息可靠性的最后防线
 *
 * 一个 main 跑完整闭环：发 3 条消息（其中 1 条"毒丸"永远失败）
 *   → 正常消息被消费
 *   → 毒丸被重试投递（观察 重试次数 0→1→2）
 *   → 达到重试上限 → 进入死信队列 %DLQ%组名
 *   → 死信消费者把它捞出来"人工兜底"
 *
 * 【知识点】
 * 1. 消费失败的正确申报方式：
 *    - 代码抛异常：客户端兜底 catch，自动按失败处理（走重试）
 *    - 业务失败（不抛异常的那种）：必须手动 return RECONSUME_LATER
 *    - 人祸写法：catch 住异常打个日志就返回 SUCCESS —— 静默丢消息！
 * 2. 书签照样推进：失败消息不卡队列，而是复制进 %RETRY%组名 重试 topic
 * 3. 重试间隔：10s 30s 1m 2m 3m ... 2h（注意与发送侧延迟 18 级不是同一张表）
 * 4. maxReconsumeTimes：并发消费默认 16 次；演示设为 2 次缩短周期
 * 5. 超过上限 → %DLQ%组名 死信队列，不再自动重试，等人工兜底
 *    （dashboard Topic 列表会出现 %RETRY%xxx 和 %DLQ%xxx，跑完去看！）
 *
 * 【运行顺序】直接跑本类。全程约 1 分钟：毒丸经历 10s、30s 两次重试后进死信。
 * 【预期输出】毒丸被投递 3 次（重试次数=0/1/2），正常消息秒过，最后死信消费者收到。
 */
public class RetryAndDlqDemo {

    private static final String GROUP = "retry-consumer-group";
    private static final String TOPIC = "BasicTopic_Retry";

    /** 等毒丸走完：3 次投递(原+重试2) */
    private static final CountDownLatch POISON_LATCH = new CountDownLatch(3);
    /** 等死信落地 */
    private static final CountDownLatch DLQ_LATCH = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        // ── 1. 业务消费者：毒丸永远返回 RECONSUME_LATER ──────────────────
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(GROUP);
        consumer.setNamesrvAddr(MqConst.NAMESRV_ADDR);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        // 演示用：把默认 16 次压到 2 次，否则要等几个小时才能看到死信
        consumer.setMaxReconsumeTimes(2);

        consumer.subscribe(TOPIC, "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                             ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    String body = new String(msg.getBody(), StandardCharsets.UTF_8);
                    if (body.contains("poison")) {
                        // 业务失败的正确姿势：主动申报"稍后重试"
                        // （也可以直接抛异常，客户端兜底后效果等价）
                        System.out.printf("[业务消费] 第 %d 次收到毒丸，返回 RECONSUME_LATER%n",
                                msg.getReconsumeTimes());
                        POISON_LATCH.countDown();
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                    System.out.printf("[业务消费] 正常消息秒过: %s (重试次数=%d)%n",
                            body, msg.getReconsumeTimes());
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        System.out.println("业务消费者已启动（maxReconsumeTimes=2）");

        // ── 2. 死信消费者：订阅 %DLQ%组名，扮演"人工兜底" ─────────────────
        DefaultMQPushConsumer dlqConsumer = new DefaultMQPushConsumer("dlq-watchdog-group");
        dlqConsumer.setNamesrvAddr(MqConst.NAMESRV_ADDR);
        dlqConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        // 死信队列的名字是固定格式：%DLQ% + 消费者组名
        dlqConsumer.subscribe("%DLQ%" + GROUP, "*");
        dlqConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                             ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    System.out.printf("[死信兜底] 收到死信! topic=%s, body=%s%n",
                            msg.getTopic(), new String(msg.getBody(), StandardCharsets.UTF_8));
                    DLQ_LATCH.countDown();
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        dlqConsumer.start();
        System.out.println("死信消费者已启动，盯着 %DLQ%" + GROUP);

        TimeUnit.SECONDS.sleep(3);

        // ── 3. 发 3 条：2 正常 + 1 毒丸 ───────────────────────────────────
        DefaultMQProducer producer = new DefaultMQProducer("retry-producer-group");
        producer.setNamesrvAddr(MqConst.NAMESRV_ADDR);
        producer.start();
        try {
            for (String body : new String[]{"normal-1", "poison-1", "normal-2"}) {
                Message msg = new Message(TOPIC, "TagR", "key-" + body,
                        body.getBytes(StandardCharsets.UTF_8));
                producer.send(msg);
                System.out.println("[发送] " + body);
            }
        } finally {
            producer.shutdown();
        }

        // ── 4. 等毒丸 3 次投递 + 死信落地（约 10s+30s 后） ─────────────────
        POISON_LATCH.await(2, TimeUnit.MINUTES);
        boolean dlqOk = DLQ_LATCH.await(2, TimeUnit.MINUTES);
        System.out.println(dlqOk
                ? "闭环完成：毒丸重试 2 次失败 -> 已进死信队列被兜底"
                : "2 分钟内没等到死信（可去 dashboard Topic 列表找 %DLQ%" + GROUP + "）");

        // 留 6 秒让位点上报（dashboard 对账 diff 用，同 c01 的教训）
        TimeUnit.SECONDS.sleep(6);
        consumer.shutdown();
        dlqConsumer.shutdown();
    }
}
