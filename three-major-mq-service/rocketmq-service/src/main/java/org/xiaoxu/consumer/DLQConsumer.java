package org.xiaoxu.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @className: DLQConsumer
 * @author: xiaoxu
 * @date: 2026/4/20 22:38
 * @Version: 1.0
 * @description:
 */
@Component
@RocketMQMessageListener(
        topic = "%DLQ%email-consumer-group",
        consumerGroup = "email-dlq-consumer-group"
)
public class DLQConsumer implements RocketMQListener<String> {
    private static final Logger log = LoggerFactory.getLogger(DLQConsumer.class);

    @Override
    public void onMessage(String s) {

        log.info("receive message: {}", s);
    }
}
