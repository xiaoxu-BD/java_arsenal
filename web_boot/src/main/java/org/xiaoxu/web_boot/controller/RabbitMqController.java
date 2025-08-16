package org.xiaoxu.web_boot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.web_boot.config.RabbitMqConfig;

/**
 * @className: RabbitMqController
 * @author: xiaoxu
 * @date: 2025/6/28 18:30
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/api")
public class RabbitMqController {

    private  static final Logger logger  = LoggerFactory.getLogger(RabbitMqController.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/send")
    public String send(String msg) {

        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.ROUTING_KEY,
                msg);

        logger.info("发送消息：{}", msg);
        return "发送消息："+ msg;
    }
}
