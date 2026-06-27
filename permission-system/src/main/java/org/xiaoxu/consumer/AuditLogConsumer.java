package org.xiaoxu.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.xiaoxu.common.exception.MessageProcessException;
import org.xiaoxu.config.RabbitMQConfig;
import org.xiaoxu.mapper.SysLoginLogMapper;
import org.xiaoxu.mapper.SysOperationLogMapper;
import org.xiaoxu.mapper.SysWorkflowLogMapper;
import org.xiaoxu.pojo.SysLoginLog;
import org.xiaoxu.pojo.SysOperationLog;
import org.xiaoxu.pojo.SysWorkflowLog;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 审计日志消费者 — 从 RabbitMQ 消费日志消息并写入数据库
 * 支持幂等性：使用 Redis 判断消息是否已处理，防止重复消费
 */
@Slf4j
@Component
public class AuditLogConsumer {

    private static final String IDEMPOTENT_KEY_PREFIX = "audit:log:processed:";
    private static final long IDEMPOTENT_EXPIRE_HOURS = 24;

    @Resource
    private SysLoginLogMapper loginLogMapper;

    @Resource
    private SysOperationLogMapper operationLogMapper;

    @Resource
    private SysWorkflowLogMapper workflowLogMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 判断消息是否已处理（幂等性检查）
     */
    private boolean isMessageProcessed(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 标记消息已处理
     */
    private void markMessageProcessed(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        stringRedisTemplate.opsForValue().set(key, "1", IDEMPOTENT_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    /**
     * 确认消息
     */
    private void ackMessage(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            throw new MessageProcessException("消息确认失败, deliveryTag=" + deliveryTag, e);
        }
    }

    /**
     * 拒绝消息
     */
    private void nackMessage(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            throw new MessageProcessException("消息拒绝失败, deliveryTag=" + deliveryTag, e);
        }
    }

    /**
     * 消费登录日志
     */
    @RabbitListener(queues = RabbitMQConfig.LOGIN_LOG_QUEUE)
    public void handleLoginLog(@Payload SysLoginLog loginLog,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                               Channel channel) {
        String messageId = "login:" + loginLog.getUsername() + ":" + loginLog.getLoginTime();

        if (isMessageProcessed(messageId)) {
            log.info("登录日志已处理过，跳过: username={}", loginLog.getUsername());
            ackMessage(channel, deliveryTag);
            return;
        }

        try {
            loginLogMapper.insert(loginLog);
            markMessageProcessed(messageId);
            log.debug("登录日志写入成功, username={}", loginLog.getUsername());
            ackMessage(channel, deliveryTag);
        } catch (Exception e) {
            log.error("登录日志写入失败, username={}", loginLog.getUsername(), e);
            nackMessage(channel, deliveryTag);
        }
    }

    /**
     * 消费操作日志
     */
    @RabbitListener(queues = RabbitMQConfig.OPERATION_LOG_QUEUE)
    public void handleOperationLog(@Payload SysOperationLog operationLog,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                   Channel channel) {
        String messageId = "op:" + operationLog.getModule() + ":" + operationLog.getMethod()
                + ":" + operationLog.getCreateTime();

        if (isMessageProcessed(messageId)) {
            log.info("操作日志已处理过，跳过: module={}, operation={}",
                    operationLog.getModule(), operationLog.getOperation());
            ackMessage(channel, deliveryTag);
            return;
        }

        try {
            operationLogMapper.insert(operationLog);
            markMessageProcessed(messageId);
            log.debug("操作日志写入成功, module={}, operation={}", operationLog.getModule(), operationLog.getOperation());
            ackMessage(channel, deliveryTag);
        } catch (Exception e) {
            log.error("操作日志写入失败, module={}", operationLog.getModule(), e);
            nackMessage(channel, deliveryTag);
        }
    }

    /**
     * 消费工作流日志
     */
    @RabbitListener(queues = RabbitMQConfig.WORKFLOW_LOG_QUEUE)
    public void handleWorkflowLog(@Payload SysWorkflowLog workflowLog,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                  Channel channel) {
        String messageId = "workflow:" + workflowLog.getProcessInstanceId() + ":"
                + workflowLog.getTaskId() + ":" + workflowLog.getAction();

        if (isMessageProcessed(messageId)) {
            log.info("工作流日志已处理过，跳过: processInstanceId={}", workflowLog.getProcessInstanceId());
            ackMessage(channel, deliveryTag);
            return;
        }

        try {
            workflowLogMapper.insert(workflowLog);
            markMessageProcessed(messageId);
            log.debug("工作流日志写入成功, processInstanceId={}", workflowLog.getProcessInstanceId());
            ackMessage(channel, deliveryTag);
        } catch (Exception e) {
            log.error("工作流日志写入失败, processInstanceId={}", workflowLog.getProcessInstanceId(), e);
            nackMessage(channel, deliveryTag);
        }
    }
}
