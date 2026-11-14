package org.xiaoxu.rocketmq.c05order;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.xiaoxu.rocketmq.MqConst;
import java.nio.charset.StandardCharsets;

import java.util.List;

/**
 * c05-1 顺序消息——生产者：把"同一个业务键"的消息钉进同一个队列
 *
 * 【运行顺序】先跑 {@link OrderConsumer}，再跑本类。
 *
 * 【知识点】
 * 1. RocketMQ 的"顺序"是【局部顺序】：只保证单个 MessageQueue 内 FIFO，
 *    Topic 有 4 个队列、消息分散在多个队列时，跨队列没有先后保证。
 *    ——所以要做全局有序只能 1 个队列（牺牲并发），做业务有序就按业务键分队列。
 * 2. MessageQueueSelector 就是"选队列策略"：
 *    send(msg, selector, arg, timeout) 的第三个参数 arg 会原样传给 select()，
 *    这里用 orderId 对队列数取模 => 同一订单的所有步骤永远进同一个队列。
 *    （等价于 hash 分区思想：和 Kafka 按 key 分区一模一样）
 * 3. 消费端要配 MessageListenerOrderly 才真正串行（见 OrderConsumer），两头缺一不可。
 *
 * 【预期输出】3 个订单 x 5 个步骤 = 15 条；每个订单固定走同一个 queueId，
 *            步骤顺序：创建订单->支付->扣减库存->发货->确认收货。
 */
public class OrderProducer {

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(MqConst.GROUP_ORDER_PRODUCER);
        producer.setNamesrvAddr(MqConst.NAMESRV_ADDR);

        try {
            producer.start();
            System.out.println("生产者启动，按订单号分区发送顺序消息...");

            // 3 个订单，每个订单 5 个步骤
            String[] steps = {"创建订单", "支付成功", "扣减库存", "商品发货", "确认收货"};

            for (int orderId = 1001; orderId <= 1003; orderId++) {
                for (int step = 0; step < steps.length; step++) {
                    Message msg = new Message(
                            MqConst.TOPIC_ORDER,
                            "TagO",
                            "order-" + orderId,
                            String.format("订单[%d] 步骤%d/%d：%s",
                                    orderId, step + 1, steps.length, steps[step]).getBytes(StandardCharsets.UTF_8));

                    /*
                     * 带队列选择器的发送重载：send(msg, selector, arg, timeoutMillis)
                     *   arg 这里传 orderId —— 决定进哪个队列的分片键
                     */
                    SendResult result = producer.send(msg, new MessageQueueSelector() {
                        @Override
                        public MessageQueue select(List<MessageQueue> mqs, Message message, Object arg) {
                            // mqs 是该 Topic 的所有队列（默认 4 个）
                            // arg 就是我们传进来的 orderId；同一订单 => 同一余数 => 同一队列
                            Integer id = (Integer) arg;
                            int index = Math.abs(id.hashCode()) % mqs.size();
                            return mqs.get(index);
                        }
                    }, orderId, 5000);

                    System.out.printf("[发送] %s -> queueId=%s%n",
                            new String(msg.getBody(), StandardCharsets.UTF_8), result.getMessageQueue().getQueueId());
                }
            }
        } finally {
            producer.shutdown();
        }
    }
}
