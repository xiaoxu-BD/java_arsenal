package org.xiaoxu.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * demo 组的死信接盘人：重试耗尽的消息最终落在 %DLQ%demo-consumer-group
 */
@Component
@RocketMQMessageListener(topic = "%DLQ%demo-consumer-group", consumerGroup = "demo-dlq-consumer-group")
public class DemoDlqConsumer implements RocketMQListener<String> {

    private static final Logger logger = LoggerFactory.getLogger(DemoDlqConsumer.class);

    @Override
    public void onMessage(String body) {
        logger.warn("[死信] demo 组放弃的消息被我接盘: body={}（生产上这里应该是入库+告警+人工重放入口）", body);
    }
}
