package org.xiaoxu.rocketmq.c02async;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.xiaoxu.rocketmq.MqConst;
import java.nio.charset.StandardCharsets;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * c02 异步发送
 *
 * 【运行顺序】先跑 c01 的 SimpleConsumer 订阅方式看效果，或者直接跑本类——
 *            本类自带收尾：10 条全部回调完成后进程自动退出。
 *
 * 【知识点】
 * 1. 异步发送：send() 立刻返回不等 Broker，发送结果由回调线程通知——
 *    主线程继续干活，适合链路 RT 敏感的场景（如秒杀下单通知）。
 * 2. SendCallback 两个方法成对出现：
 *      onSuccess(SendResult) —— 发送成功，Broker 确认了
 *      onException(Throwable) —— 发送失败（超时/网络断/Broker 拒收），这里写补偿逻辑
 * 3. 经典坑①：主线程发完立刻 producer.shutdown() —— 回调还没回来资源先释放了，
 *    轻则丢回调，重则抛异常。所以必须"等所有回调落地再 shutdown"（CountDownLatch）。
 * 4. 经典坑②：异步发送在途请求有上限（默认 65536），超过抛
 *    RemotingTooMuchRequestException——疯狂异步发送时要做好限流/背压。
 *
 * 【预期输出】"发送请求已提交" 先噼里啪啦打完（send 没阻塞），
 *            然后 10 个 [异步回调] 交错打印（回调在另一个线程池执行）。
 */
public class AsyncProducer {

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(MqConst.GROUP_ASYNC_PRODUCER);
        producer.setNamesrvAddr(MqConst.NAMESRV_ADDR);
        producer.setSendMsgTimeout(5000);

        // 10 条消息全部回调（无论成败）后才放行 main，防止提前 shutdown 丢回调
        CountDownLatch latch = new CountDownLatch(10);

        try {
            producer.start();
            System.out.println("生产者启动成功，开始异步发送 10 条...");

            for (int i = 1; i <= 10; i++) {
                Message msg = new Message(
                        MqConst.TOPIC_FIRE,
                        "TagA",
                        "async-key-" + i,
                        ("异步消息 " + i).getBytes(StandardCharsets.UTF_8));

                /*
                 * 异步重载：send(msg, callback)。
                 * 这一行只负责"提交发送任务"，毫秒级返回，不等 Broker 确认。
                 */
                producer.send(msg, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult result) {
                        // 回调运行在客户端内部线程池（Netty IO 线程 -> 后立即转交），
                        // 不要在这里做耗时业务，避免拖垮其它消息的回调
                        System.out.printf("[异步回调-成功] msgId=%s, queueId=%s%n",
                                result.getMsgId(), result.getMessageQueue().getQueueId());
                        latch.countDown();
                    }

                    @Override
                    public void onException(Throwable e) {
                        // 实际项目：记日志 + 落库重发 / 告警。这里只打印11
                        System.out.printf("[异步回调-失败] %s%n", e.getMessage());
                        latch.countDown();
                    }
                });

                // 观察"提交即刻返回"的效果：这句紧跟在 send 后打印，不会等回调
                System.out.println("发送请求已提交：" + i);
            }

            System.out.println("10 条全部提交完毕，等待回调...");
            latch.await(60, TimeUnit.SECONDS);
        } finally {
            producer.shutdown();
            System.out.println("所有回调已落地，生产者关闭退出");
        }
    }
}
