package org.xiaoxu.rocketmq.spring.s02consume;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * s02-3 广播消费者（对照 c06 BroadcastDemo）
 *
 * messageModel = BROADCASTING = 原生 setMessageModel(MessageModel.BROADCASTING)。
 * c06 的结论全部照旧：位点存本地、没有重试、同组每个实例都收全量。
 * 单机单实例看不出广播效果——要体会的话把应用跑两个实例（不同 server.port），
 * 都会收到 http://localhost:8081/send/broadcast 发的每一条。
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "SpringTopic_Broadcast",
        consumerGroup = "spring-broadcast-consumer-group",
        messageModel = MessageModel.BROADCASTING
)
public class BroadcastSpringConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        log.info("[广播 消费] {}", message);
    }
}
