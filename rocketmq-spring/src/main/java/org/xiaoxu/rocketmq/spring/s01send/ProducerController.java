package org.xiaoxu.rocketmq.spring.s01send;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * s01 生产者：RocketMQTemplate 五种发送姿势（HTTP 触发，浏览器/curl 直接点）
 *
 * 【原生映射】RocketMQTemplate = 托管版 DefaultMQProducer：
 *   原生：自己 new + start + send + shutdown
 *   这里：starter 自动创建并 start（容器启动时），容器关闭自动 shutdown，
 *        代码里只管调 syncSend/asyncSend/sendOneWay
 *
 * 【starter 特色：destination 语法】"topic名:tag名" —— 冒号拼 tag！
 *   原生是 new Message(topic, tag, ...) 两个参数，starter 把 tag 塞进了目的地字符串。
 *
 * 【怎么跑】先启动本应用，然后浏览器依次访问：
 *   http://localhost:8081/send/sync        （对照 c01 SyncProducer）
 *   http://localhost:8081/send/async       （对照 c02 AsyncProducer）
 *   http://localhost:8081/send/oneway      （对照 c03 OnewayProducer）
 *   http://localhost:8081/send/delay       （对照 c04 DelayProducer）
 *   http://localhost:8081/send/order       （对照 c05 OrderProducer）
 *   http://localhost:8081/send/tag         （对照 c07 TagFilterProducer）
 *   http://localhost:8081/send/broadcast   （对照 c06 BroadcastDemo）
 */
@Slf4j
@RestController
public class ProducerController {

    /** starter 自动装配好的"托管生产者"，直接注入就用 */
    private final RocketMQTemplate rocketMQTemplate;

    public ProducerController(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /** 同步发送（= producer.send(msg)，阻塞等 SendResult） */
    @GetMapping("/send/sync")
    public String sync() {
        // destination "SpringTopic_Sync:TagA" = 原生 new Message("SpringTopic_Sync", "TagA", ...)
        SendResult result = rocketMQTemplate.syncSend("SpringTopic_Sync:TagA",
                MessageBuilder.withPayload("Hello Spring 同步消息").build());

        log.info("[同步] status={}, msgId={}, queue={}",
                result.getSendStatus(), result.getMsgId(), result.getMessageQueue().getQueueId());
        return "同步发送: " + result.getSendStatus() + " " + result.getMsgId();
    }

    /** 异步发送（= producer.send(msg, SendCallback)，注意 starter 同样要考虑回调时机） */
    @GetMapping("/send/async")
    public String async() {
        rocketMQTemplate.asyncSend("SpringTopic_Sync:TagA",
                MessageBuilder.withPayload("Hello Spring 异步消息").build(),
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult result) {
                        log.info("[异步回调-成功] msgId={}", result.getMsgId());
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("[异步回调-失败] {}", e.getMessage());
                    }
                });
        // 与原生 c02 同款坑：回调在异步线程，HTTP 响应先返回，回调结果只能看应用日志
        return "异步已提交，回调结果看控制台日志";
    }

    /** 单向发送（= producer.sendOneway(msg)，无回执） */
    @GetMapping("/send/oneway")
    public String oneway() {
        rocketMQTemplate.sendOneWay("SpringTopic_Sync:TagB",
                MessageBuilder.withPayload("Hello Spring 单向消息").build());
        return "单向发送完成（没有回执）";
    }

    /** 延迟消息（= msg.setDelayTimeLevel(3)，10 秒后投递） */
    @GetMapping("/send/delay")
    public String delay() {
        // syncSend 的重载：destination, message, timeout, delayLevel
        rocketMQTemplate.syncSend("SpringTopic_Delay",
                MessageBuilder.withPayload("10 秒后见").build(),
                3000, 3);   // 3 = 10s（还是原生那张 18 级表）

        return "延迟消息已发送：10 秒后投递";
    }

    /** 顺序消息（= producer.send(msg, MessageQueueSelector, orderId, timeout)） */
    @GetMapping("/send/order")
    // 【SB3.2 坑】Spring 6.1 起不再从字节码读参数名，@RequestParam 不写 name 直接 500：
    // "Name for argument of type [int] not specified... '-parameters' flag"
    public String order(@RequestParam(name = "orderId", defaultValue = "1001") int orderId) {
        String[] steps = {"创建订单", "支付成功", "扣减库存", "商品发货", "确认收货"};
        for (String step : steps) {
            // syncSendOrderly 内部就是 MessageQueueSelector：hashKey 相同 -> 同一队列
            rocketMQTemplate.syncSendOrderly("SpringTopic_Order",
                    MessageBuilder.withPayload("订单[" + orderId + "] " + step).build(),
                    String.valueOf(orderId));   // hashKey = 分片键（原生里的 select 参数）
        }
        return "订单 " + orderId + " 的 5 个步骤已按序发往同一队列";
    }

    /** Tag 过滤演示数据（发 TagA/TagB/TagC 各 3 条） */
    @GetMapping("/send/tag")
    public String tag() {
        String[] tags = {"TagA", "TagB", "TagC"};
        for (int i = 1; i <= 9; i++) {
            rocketMQTemplate.syncSend("SpringTopic_Tag:" + tags[i % 3],
                    MessageBuilder.withPayload("消息 " + i).build());
        }
        return "9 条混合 tag 消息已发送（消费者只订了 TagA || TagB）";
    }

    /** 广播消息（发 3 条，广播消费者的每个实例都会全收） */
    @GetMapping("/send/broadcast")
    public String broadcast() {
        for (int i = 1; i <= 3; i++) {
            rocketMQTemplate.syncSend("SpringTopic_Broadcast",
                    MessageBuilder.withPayload("广播消息 " + i).build());
        }
        return "3 条广播消息已发送";
    }
}
