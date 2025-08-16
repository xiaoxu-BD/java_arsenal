package org.xiaoxu.web_boot.service;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.xiaoxu.web_boot.config.RabbitMqConfig;

import java.io.IOException;

/**
 * @className: ConsumerService
 * @author: xiaoxu
 * @date: 2025/6/28 18:43
 * @Version: 1.0
 * @description:
 */
@Component
@Slf4j
public class ConsumerService {

    @RabbitListener(queues = RabbitMqConfig.QUEUE_NAME,ackMode = "MANUAL")
    public void receive(String message, Channel channel, Message amqpMessage) throws IOException {
        log.info("receive message: {}", message);
        // 模拟异常，消息不被确认
        if (message.contains("fail")){
            throw new RuntimeException("fail");
        }
        try {
            //确认消息
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            // 拒绝消息，不重回队列，避免死循环
            channel.basicReject(amqpMessage.getMessageProperties().getDeliveryTag(), false);
            throw new RuntimeException(e);
        }
    }
}
