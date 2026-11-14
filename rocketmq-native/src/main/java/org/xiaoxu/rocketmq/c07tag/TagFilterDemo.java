package org.xiaoxu.rocketmq.c07tag;

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
 * c07 Tag 过滤（服务端过滤）
 *
 * 一个 main 演示全流程：先挂一个只订阅 TagA || TagB 的消费者，再发
 * TagA x3 + TagB x3 + TagC x3 共 9 条，消费者只收到 TagA/TagB 的 6 条。
 *
 * 【知识点】
 * 1. Tag 是 Topic 下的二级分类：一个"订单Topic"里，TagA=创建、TagB=支付、TagC=取消。
 * 2. subscribe 的第二参数 tag 表达式：
 *      "TagA"          只要 TagA
 *      "TagA || TagB"  TagA 或 TagB（注意是 || 不是单个 |）
 *      "TagA && TagB"  同时带两个 tag（一条消息只能有一个 tag，基本用不上）
 *      "*" / null      全收
 * 3. 过滤发生在【Broker 端】（服务器按订阅关系筛完再投），不是消费者拉回来再丢——
 *    省网络带宽。原理：消费者把 tag 表达式的 hashcode 先传给 Broker 粗筛，
 *    拉回后再做精确比对（极端 hash 碰撞会多放行，客户端精确过滤兜底）。
 * 4. 进阶预告：SQL92 过滤（"a > 5"这种按消息用户属性过滤，需 Broker 开
 *    enablePropertyFilter=true），放在进阶篇再练。
 *
 * 【预期输出】只打印 TagA x3 + TagB x3 共 6 条；TagC 的 3 条虽然发送成功，
 *            但一条都不会出现在消费者端（被服务端过滤掉了）。
 */
public class TagFilterDemo {

    /** 订阅 TagA/TagB 的消费者应收到 3+3=6 条 */
    private static final CountDownLatch LATCH = new CountDownLatch(6);

    public static void main(String[] args) throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MqConst.GROUP_TAG_CONSUMER);
        consumer.setNamesrvAddr(MqConst.NAMESRV_ADDR);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        // 核心一行：订阅表达式——只要 TagA 或 TagB，TagC 不想要
        consumer.subscribe(MqConst.TOPIC_TAG, "TagA || TagB");

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                             ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    // getTags() 拿到消息上的 tag，验证确实没有 TagC 混进来
                    System.out.printf("[收到] tag=%s, body=%s%n",
                            msg.getTags(), new String(msg.getBody(), StandardCharsets.UTF_8));
                    LATCH.countDown();
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
        System.out.println("消费者已启动（只订阅 TagA || TagB），3 秒后发送 9 条混合 tag 消息...");
        TimeUnit.SECONDS.sleep(3);

        sendMixedTags();

        LATCH.await(60, TimeUnit.SECONDS);
        System.out.println("演示结束：TagC 的 3 条一条都没收到（被 Broker 在服务端过滤掉了）");

        /*
         * 【dashboard 对账知识点】跑完后去 Consumer 页看 BasicTopic_Tag：
         * 1. brokerOffset 总和 = 9（TagA3+TagB3+TagC3 都进了 broker）
         * 2. consumerOffset 也会推进到 9、diff=0 —— 但 TagC 并没有被消费！
         *    因为 offset 记录的是"拉取进度"：拉取时 Broker 按 tag 粗筛跳过 TagC，
         *    客户端 correctTagsOffset 直接把位点翻到 nextBeginOffset。
         *    => 推论：这个组以后就算改订阅要 TagC，历史 TagC 也拉不回来了（位点已越过）。
         * 3. 若个别 queue 还显示 diff=1：位点 5 秒异步提交没赶上，等几秒刷新即归零。
         */
        TimeUnit.SECONDS.sleep(6);
        consumer.shutdown();
    }

    private static void sendMixedTags() throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(MqConst.GROUP_TAG_PRODUCER);
        producer.setNamesrvAddr(MqConst.NAMESRV_ADDR);
        producer.start();
        try {
            String[] tags = {"TagA", "TagB", "TagC"};
            for (int i = 1; i <= 9; i++) {
                // 轮流用 3 种 tag 发送 => TagA x3、TagB x3、TagC x3
                String tag = tags[i % 3];
                Message msg = new Message(
                        MqConst.TOPIC_TAG,
                        tag,
                        "tag-key-" + i,
                        ("消息 " + i + "（带 " + tag + "）").getBytes(StandardCharsets.UTF_8));
                producer.send(msg);
                System.out.printf("[发送] tag=%s%n", tag);
            }
        } finally {
            producer.shutdown();
        }
    }
}
