package org.xiaoxu.web_boot.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @className: RabbitMqConfig
 * @author: xiaoxu
 * @date: 2025/6/28 18:25
 * @Version: 1.0
 * @description:
 */
@Configuration
public class RabbitMqConfig {

    //定义路由键
    public  static final  String ROUTING_KEY = "xiaoxu";
    //定义队列
    public  static final  String QUEUE_NAME = "xiaoxu_queue";

    //定义交换机
    public  static final  String EXCHANGE_NAME = "xiaoxu_exchange";


    //声明队列
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME,true);
    }

    //交换机
    @Bean
    public Exchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    //绑定关系
    @Bean
    public Binding binding(Queue queue,Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }

}
