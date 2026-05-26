package org.xiaoxu.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VerificationCodeMQConfig {

    public static final String EXCHANGE = "verify.code.exchange";
    public static final String QUEUE = "verify.code.queue";
    public static final String ROUTING_KEY = "verify.code.send";
    public static final String DEAD_EXCHANGE = "verify.code.dead.exchange";
    public static final String DEAD_QUEUE = "verify.code.dead.queue";
    public static final String DEAD_ROUTING_KEY = "verify.code.dead";

//    声明交换机类型
    @Bean
    public DirectExchange verifyCodeExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue verifyCodeQueue() {
        return QueueBuilder.durable(QUEUE)
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(DEAD_ROUTING_KEY)
                .ttl(60000)
                .build();
    }

    @Bean
    public Binding verifyCodeBinding(Queue verifyCodeQueue, DirectExchange verifyCodeExchange) {
        return BindingBuilder.bind(verifyCodeQueue).to(verifyCodeExchange).with(ROUTING_KEY);
    }

    @Bean
    public DirectExchange deadExchange() {
        return new DirectExchange(DEAD_EXCHANGE, true, false);
    }

    @Bean
    public Queue deadQueue() {
        return QueueBuilder.durable(DEAD_QUEUE).build();
    }

    @Bean
    public Binding deadBinding(Queue deadQueue, DirectExchange deadExchange) {
        return BindingBuilder.bind(deadQueue).to(deadExchange).with(DEAD_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
