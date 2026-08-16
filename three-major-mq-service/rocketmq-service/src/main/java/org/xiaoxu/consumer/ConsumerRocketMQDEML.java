package org.xiaoxu.consumer;

import org.apache.rocketmq.common.message.MessageClientExt;
import org.apache.rocketmq.common.message.MessageExt;
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
 * @description: 用 MessageExt 原始消息类型消费，同框展示三个标识符与投递次数
 */
@Service
@RocketMQMessageListener(topic = "rocket-service-exm", consumerGroup = "demo-consumer-group",maxReconsumeTimes = 1)
public class ConsumerRocketMQDEML implements RocketMQListener<MessageExt> {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerRocketMQDEML.class);

    @Override
    public void onMessage(MessageExt msg) {
        String body = new String(msg.getBody());
        if (body.startsWith("poison")) {
            logger.warn("[消费者] 模拟业务失败：业务键={} 第{}次投递，抛异常触发重试",
                    msg.getKeys(), msg.getReconsumeTimes());
            throw new RuntimeException("模拟消费失败");
        }
        logger.info("[消费者] 线程 {} UNIQ_KEY={} offsetMsgId={} 业务键={} 第{}次投递 body={}",
                Thread.currentThread().getName(),
                msg.getMsgId(),
                ((MessageClientExt) msg).getOffsetMsgId(),
                msg.getKeys(),
                msg.getReconsumeTimes(),
                body);
    }
}
