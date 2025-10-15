package org.xiaoxu.consumer;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xiaoxu.message.Demo1Message;

@Component
@RocketMQMessageListener(
        topic = Demo1Message.TOPIC,
        consumerGroup = "demo01-A-consumer-group-" + Demo1Message.TOPIC
)
public class Demo01AConsumer implements RocketMQListener<MessageExt> {


    private static final Logger log = LoggerFactory.getLogger(Demo01AConsumer.class);

    @Override
    public void onMessage(MessageExt message) {
        log.info("[onMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), message);
    }

}