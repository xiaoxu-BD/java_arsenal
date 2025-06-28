package org.xiaoxu.web_boot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.xiaoxu.web_boot.config.RabbitMqConfig;

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

    @RabbitListener(queues = RabbitMqConfig.QUEUE_NAME)
    public void receive(String message) {
        log.info("receive message: {}", message);
    }
}
