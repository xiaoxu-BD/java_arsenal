package org.xiaoxu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @className: RabbitMQConfig
 * @author: xiaoxu
 * @date: 2025/11/13 10:24
 * @Version: 1.0
 * @description:
 */
@Configuration
public class RabbitMQConfig {


    public static final String EXCHANGE_NAME = "registerExchange";

    public static final String QUEUE_NAME = "registerQueue";

    public static final String ROUTING_KEY = "emailRouting";



    // 死信交换机
    public static final String DEAD_EXCHANGE = "dead.exchange";
    public static final String DEAD_QUEUE = "dead.queue";
    public static final String DEAD_ROUTING_KEY = "dead.key";



    // 正常交换机
    public static final String NORMAL_EXCHANGE = "normal.exchange";
    public static final String NORMAL_QUEUE = "normal.queue";
    public static final String NORMAL_ROUTING_KEY = "normal.key";




    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);


    @Bean
    public Queue registerQueue(){
        //  durable: 是否持久化
        return new Queue(QUEUE_NAME,true);
    }



    @Bean
    public DirectExchange registerExchange(){
        //  durable: 是否持久化  autoDelete: 是否自动删除
        return new DirectExchange(EXCHANGE_NAME,true,false);
    }

    @Bean
    public DirectExchange deadExchange() {
        return ExchangeBuilder.directExchange(DEAD_EXCHANGE).durable(true).build();
    }



    @Bean
    public DirectExchange normalExchange() {
        return ExchangeBuilder.directExchange(NORMAL_EXCHANGE).durable(true).build();
    }
    @Bean
    public Queue normalQueue() {
        Map<String, Object> args = new HashMap<>();
        // 指定死信交换机和路由键
        args.put("x-dead-letter-exchange", DEAD_EXCHANGE);
        args.put("x-dead-letter-routing-key", DEAD_ROUTING_KEY);
        // 设置消息过期时间 (5秒)
        args.put("x-message-ttl", 5000);
        return QueueBuilder.durable(NORMAL_QUEUE).withArguments(args).build();
    }


    @Bean
    public Queue deadQueue() {
        return QueueBuilder.durable(DEAD_QUEUE).build();
    }


    @Bean
    public Binding binding(){
        return BindingBuilder.bind(registerQueue()).to(registerExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding deadBinding() {
        return BindingBuilder.bind(deadQueue()).to(deadExchange()).with(DEAD_ROUTING_KEY);
    }

    @Bean
    public Binding normalBinding() {
        return BindingBuilder.bind(normalQueue()).to(normalExchange()).with(NORMAL_ROUTING_KEY);
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        //关键!!!
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack){
                log.error("消息发送失败：" + cause + ":" + correlationData);
            }
        });

        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            // 处理返回消息
            log.info("返回消息：" + returnedMessage.getMessage());
            log.info("路由键：" + returnedMessage.getRoutingKey());
            log.info("交换机：" + returnedMessage.getExchange());
            log.info("状态码：" + returnedMessage.getReplyCode());
            log.info("状态信息：" + returnedMessage.getReplyText());
        });
        return rabbitTemplate;
    }



}
