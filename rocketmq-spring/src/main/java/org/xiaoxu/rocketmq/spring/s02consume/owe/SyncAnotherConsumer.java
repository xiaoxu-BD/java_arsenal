package org.xiaoxu.rocketmq.spring.s02consume.owe;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "SpringTopic_Sync",consumeMode = ConsumeMode.CONCURRENTLY, consumerGroup = "spring-other-consumer-group"
,selectorExpression = "*" , messageModel = MessageModel.CLUSTERING)
public class SyncAnotherConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String message) {

        try {
            Thread.sleep(1000);
            log.info("[单独开了一个也能消费],message {}",message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
