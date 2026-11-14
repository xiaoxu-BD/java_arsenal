package org.xiaoxu.rocketmq.spring.s02consume;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * s02-4 顺序消费者（对照 c05 OrderConsumer——你还没跑的原生案例，正好在注解版补上）
 *
 * consumeMode = ORDERLY = 原生 registerMessageListener(MessageListenerOrderly)：
 * 队列内串行消费 + 分布式锁。配合生产端 syncSendOrderly(同 hashKey -> 同队列)。
 *
 * 实验：访问 http://localhost:8081/send/order?orderId=1001 再换 1002、1003 各发一次，
 * 观察日志：同一订单的 5 步永远 1->2->3->4->5，且线程名对同一订单固定。
 * 试着把 consumeMode 改成 CONCURRENTLY 多刷几次——亲眼看乱序（这就是 c05 留的实验）。
 *
 * 注意：ORDERLY 下 consume 抛异常 = 原生返回 SUSPEND_CURRENT_QUEUE_A_MOMENT，
 * 当前队列挂起重试（不能重新入队否则乱序），毒丸消息会堵住整个队列——慎重抛异常。
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "SpringTopic_Order",
        consumerGroup = "spring-order-consumer-group",
        consumeMode = ConsumeMode.ORDERLY   // 顺序消费：队列内单线程串行
)
public class OrderSpringConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        log.info("[顺序消费] [{}] {}", Thread.currentThread().getName(), message);
    }
}
