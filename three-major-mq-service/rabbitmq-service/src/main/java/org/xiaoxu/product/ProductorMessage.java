package org.xiaoxu.product;

import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xiaoxu.config.RabbitMQConfig;

/**
 * @className: ProductorMessage
 * @author: xiaoxu
 * @date: 2025/11/13 10:30
 * @Version: 1.0
 * @description:
 */
@Component
public class ProductorMessage {


    @Autowired
    private RabbitTemplate rabbitTemplate;




    public void sendMessage(String message){
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,RabbitMQConfig.ROUTING_KEY,message);
    }

    public void sendNoBindingRouteMessage(String message){

        String routeKey = "noBindingRouting";


        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,routeKey,message);
    }


    public void sendNoExchangeMessage(String message){
        String exchangeName = "noExchange";
        rabbitTemplate.convertAndSend(exchangeName,RabbitMQConfig.ROUTING_KEY,message);
    }


    public void sendMessageToNormal(String msg){
        rabbitTemplate.convertAndSend(RabbitMQConfig.NORMAL_EXCHANGE,RabbitMQConfig.NORMAL_ROUTING_KEY,msg);
    }
}

