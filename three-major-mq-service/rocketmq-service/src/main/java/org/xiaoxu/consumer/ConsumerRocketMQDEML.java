package org.xiaoxu.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @className: ConsumerRocketMQDEML
 * @author: xiaoxu
 * @date: 2026/4/18 23:27
 * @Version: 1.0
 * @description:
 */
@Service
@RocketMQMessageListener(topic = "rocket-service-exm", consumerGroup = "demo-consumer-group",maxReconsumeTimes = 1)
public class ConsumerRocketMQDEML implements RocketMQListener<String> {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerRocketMQDEML.class);
    @Override
    public void onMessage(String msg) {
        logger.info("msg is : {}", msg);
    }
}
