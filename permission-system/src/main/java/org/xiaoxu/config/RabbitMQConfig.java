package org.xiaoxu.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类 — 审计日志异步处理
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    /**
     * 审计日志交换机
     */
    public static final String AUDIT_LOG_EXCHANGE = "audit.log.exchange";

    /**
     * 登录日志队列
     */
    public static final String LOGIN_LOG_QUEUE = "audit.login.log.queue";

    /**
     * 操作日志队列
     */
    public static final String OPERATION_LOG_QUEUE = "audit.operation.log.queue";

    /**
     * 工作流日志队列
     */
    public static final String WORKFLOW_LOG_QUEUE = "audit.workflow.log.queue";

    /**
     * 登录日志路由键
     */
    public static final String LOGIN_LOG_ROUTING_KEY = "audit.log.login";

    /**
     * 操作日志路由键
     */
    public static final String OPERATION_LOG_ROUTING_KEY = "audit.log.operation";

    /**
     * 工作流日志路由键
     */
    public static final String WORKFLOW_LOG_ROUTING_KEY = "audit.log.workflow";

    // ==================== 消息转换器 ====================

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ==================== 消费者容器工厂 ====================

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(1);
        return factory;
    }

    // ==================== 交换机 ====================

    @Bean
    public DirectExchange auditLogExchange() {
        return new DirectExchange(AUDIT_LOG_EXCHANGE, true, false);
    }

    // ==================== 队列 ====================

    @Bean
    public Queue loginLogQueue() {
        return QueueBuilder.durable(LOGIN_LOG_QUEUE)
                .withArgument("x-message-ttl", 60000) // 消息 TTL 60秒
                .build();
    }

    @Bean
    public Queue operationLogQueue() {
        return QueueBuilder.durable(OPERATION_LOG_QUEUE)
                .withArgument("x-message-ttl", 60000)
                .build();
    }

    @Bean
    public Queue workflowLogQueue() {
        return QueueBuilder.durable(WORKFLOW_LOG_QUEUE)
                .withArgument("x-message-ttl", 60000)
                .build();
    }

    // ==================== 绑定 ====================

    @Bean
    public Binding loginLogBinding(Queue loginLogQueue, DirectExchange auditLogExchange) {
        return BindingBuilder.bind(loginLogQueue).to(auditLogExchange).with(LOGIN_LOG_ROUTING_KEY);
    }

    @Bean
    public Binding operationLogBinding(Queue operationLogQueue, DirectExchange auditLogExchange) {
        return BindingBuilder.bind(operationLogQueue).to(auditLogExchange).with(OPERATION_LOG_ROUTING_KEY);
    }

    @Bean
    public Binding workflowLogBinding(Queue workflowLogQueue, DirectExchange auditLogExchange) {
        return BindingBuilder.bind(workflowLogQueue).to(auditLogExchange).with(WORKFLOW_LOG_ROUTING_KEY);
    }

    // ==================== RabbitTemplate ====================

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 设置 JSON 消息转换器
        rabbitTemplate.setMessageConverter(jsonMessageConverter);

        // 消息发送到交换机确认
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("消息发送到交换机失败, correlationData={}, cause={}", correlationData, cause);
            }
        });

        // 消息从交换机路由到队列失败
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("消息路由到队列失败, exchange={}, routingKey={}, replyText={}",
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyText());
        });

        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }
}
