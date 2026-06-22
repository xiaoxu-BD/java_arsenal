package org.xiaoxu.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.xiaoxu.config.RabbitMQConfig;
import org.xiaoxu.mapper.SysLoginLogMapper;
import org.xiaoxu.mapper.SysOperationLogMapper;
import org.xiaoxu.mapper.SysWorkflowLogMapper;
import org.xiaoxu.pojo.SysLoginLog;
import org.xiaoxu.pojo.SysOperationLog;
import org.xiaoxu.pojo.SysWorkflowLog;

import jakarta.annotation.Resource;
import java.io.IOException;

/**
 * 审计日志消费者 — 从 RabbitMQ 消费日志消息并写入数据库
 */
@Slf4j
@Component
public class AuditLogConsumer {

    @Resource
    private SysLoginLogMapper loginLogMapper;

    @Resource
    private SysOperationLogMapper operationLogMapper;

    @Resource
    private SysWorkflowLogMapper workflowLogMapper;

    /**
     * 消费登录日志
     */
    @RabbitListener(queues = RabbitMQConfig.LOGIN_LOG_QUEUE)
    public void handleLoginLog(@Payload SysLoginLog loginLog,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                               Channel channel) {
        try {
            loginLogMapper.insert(loginLog);
            log.debug("登录日志写入成功, username={}", loginLog.getUsername());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("登录日志写入失败, username={}", loginLog.getUsername(), e);
            try {
                // 拒绝消息，不重新入队
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("ACK 失败", ex);
            }
        }
    }

    /**
     * 消费操作日志
     */
    @RabbitListener(queues = RabbitMQConfig.OPERATION_LOG_QUEUE)
    public void handleOperationLog(@Payload SysOperationLog operationLog,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                   Channel channel) {
        try {
            operationLogMapper.insert(operationLog);
            log.debug("操作日志写入成功, module={}, operation={}", operationLog.getModule(), operationLog.getOperation());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("操作日志写入失败, module={}", operationLog.getModule(), e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("ACK 失败", ex);
            }
        }
    }

    /**
     * 消费工作流日志
     */
    @RabbitListener(queues = RabbitMQConfig.WORKFLOW_LOG_QUEUE)
    public void handleWorkflowLog(@Payload SysWorkflowLog workflowLog,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                  Channel channel) {
        try {
            workflowLogMapper.insert(workflowLog);
            log.debug("工作流日志写入成功, processInstanceId={}", workflowLog.getProcessInstanceId());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("工作流日志写入失败, processInstanceId={}", workflowLog.getProcessInstanceId(), e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("ACK 失败", ex);
            }
        }
    }
}
