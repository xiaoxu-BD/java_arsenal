package org.xiaoxu.rocketmq.c01sync;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.xiaoxu.rocketmq.MqConst;
import java.nio.charset.StandardCharsets;

/**
 * c01-1 同步发送（最基础的 Hello World）
 * 【运行顺序】先启动本类发 3 条消息，再启动 {@link SimpleConsumer} 消费；
 *            也可以反过来（消费者先挂着等消息，更接近真实项目）。
 * 【知识点】
 * 1. 同步发送：send() 阻塞，等 Broker 明确返回"收到"才继续，可靠性最高、性能最低，
 *    适合重要消息：订单、支付。
 * 2. 生产者三步曲：创建 -> start() -> send()，用完 shutdown()（会断开连接、释放资源）。
 * 3. SendResult.sendStatus 三种非 OK 状态的含义：
 *    FLUSH_DISK_TIMEOUT   刷盘超时（Broker 没在规定时间内把消息写进磁盘）
 *    FLUSH_SLAVE_TIMEOUT  主从同步超时
 *    SLAVE_NOT_AVAILABLE  从节点不可用
 *    ——业务代码一般检查 != SEND_OK 就走补偿逻辑（重发/告警/记库）。
 * 【预期输出】3 行 [同步发送] msgId=7F000001... status=SEND_OK queueId=x，
 *            queueId 在 0~3 之间随机（默认 4 个队列轮询写）。
 */
@Slf4j
public class SyncProducer {

    public static void main(String[] args) throws Exception {
        /*
         * 构造参数是"生产者组名"：同一组内多个生产者实例只是一种逻辑分组。
         * 普通消息场景组名作用不大，但事务消息回查时 Broker 靠它反查生产者，所以必填。
         */
        DefaultMQProducer producer = new DefaultMQProducer(MqConst.GROUP_SYNC_PRODUCER);


        // 告诉客户端去哪找 NameServer（路由中心），多个用分号隔开：ip1:9876;ip2:9876
        producer.setNamesrvAddr(MqConst.NAMESRV_ADDR);

        // 发送超时：默认 3000ms，本地学习够用；生产环境按网络情况调大
        producer.setSendMsgTimeout(5000);

        try {
            // start() 内部会：连 NameServer、拉取路由、启动一堆后台线程（定时器、netty 等）
            producer.start();
            System.out.println("生产者启动成功，开始同步发送...");

            for (int i = 1; i <= 3; i++) {
                /*
                 * Message 四要素：
                 *   topic  消息发到哪（一级分类）
                 *   tag    二级分类，消费端按 tag 过滤（c07 专门练）
                 *   keys   业务唯一键（如订单号）。Broker 会建索引，
                 *          出问题时可按 key 在控制台/日志里精确查这条消息
                 *   body  消息体，只能是 byte[]，对象需自己序列化（JSON 等）
                 */

                Message msg = new Message(
                        MqConst.TOPIC_SYNC,
                        "TagA",
                        "order-key-" + i,
                        ("hello rocketmq 同步消息" + i ).getBytes(StandardCharsets.UTF_8));

                // 同步发送：线程阻塞在这里，直到 Broker 返回确认
                SendResult result = producer.send(msg);

                 // SEND_OK 才算真正成功
                log.info("[同步发送] msgId= {}, status={} , queueId={}",result.getMsgId(),result.getSendStatus(),result.getMessageQueue().getQueueId());
            }
        } finally {
            // shutdown 放 finally：哪怕发送抛异常也保证释放资源
            producer.shutdown();
            log.info("生产者已关闭，进程退出");
        }
    }
}
