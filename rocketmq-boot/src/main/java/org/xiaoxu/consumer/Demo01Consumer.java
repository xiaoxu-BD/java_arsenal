package org.xiaoxu.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xiaoxu.message.Demo1Message;

/**
 * @className: Demo01Consumer
 * @author: xiaoxu
 * @date: 2025/10/4 22:16
 * @Version: 1.0
 * @description:
 */
@Component
@RocketMQMessageListener(topic = Demo1Message.TOPIC, consumerGroup = "demo01-consumer-group-" + Demo1Message.TOPIC)
public class Demo01Consumer implements RocketMQListener<Demo1Message> {
    private static final Logger log = LoggerFactory.getLogger(Demo01Consumer.class);

    @Override
    public void onMessage(Demo1Message demo1Message) {
        log.info("[onMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), demo1Message);
    }
}
