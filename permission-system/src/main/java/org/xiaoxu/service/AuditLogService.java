package org.xiaoxu.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.xiaoxu.config.RabbitMQConfig;
import org.xiaoxu.mapper.SysLoginLogMapper;
import org.xiaoxu.mapper.SysOperationLogMapper;
import org.xiaoxu.mapper.SysWorkflowLogMapper;
import org.xiaoxu.pojo.SysLoginLog;
import org.xiaoxu.pojo.SysOperationLog;
import org.xiaoxu.pojo.SysWorkflowLog;

import java.time.LocalDateTime;

/**
 * 审计日志服务 — 通过 RabbitMQ 异步写入日志
 */
@Slf4j
@Service
public class AuditLogService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private SysLoginLogMapper loginLogMapper;

    @Resource
    private SysOperationLogMapper operationLogMapper;

    @Resource
    private SysWorkflowLogMapper workflowLogMapper;

    /**
     * 记录登录日志（异步）
     */
    public void recordLoginLog(String username, String loginType, String ip,
                               String browser, String os, int status, String message) {
        SysLoginLog loginLog = buildLoginLog(username, loginType, ip, status, message);
        loginLog.setWriteType(1); // MQ 异步写入

        try {
            // 发送到 MQ
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.AUDIT_LOG_EXCHANGE,
                    RabbitMQConfig.LOGIN_LOG_ROUTING_KEY,
                    loginLog
            );
            log.info("登录日志已发送到MQ, username={}", username);
        } catch (Exception e) {
            log.error("发送登录日志到MQ失败，降级为同步写入, username={}", username, e);
            // 降级：同步写入
            loginLog.setWriteType(0); // 同步写入
            fallbackRecordLoginLog(loginLog);
        }
    }

    /**
     * 记录业务操作日志（异步）
     */
    public void recordOperationLog(SysOperationLog opLog) {
        if (opLog.getCreateTime() == null) {
            opLog.setCreateTime(LocalDateTime.now());
        }
        opLog.setWriteType(1); // MQ 异步写入

        try {
            // 发送到 MQ
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.AUDIT_LOG_EXCHANGE,
                    RabbitMQConfig.OPERATION_LOG_ROUTING_KEY,
                    opLog
            );
            log.info("操作日志已发送到MQ, module={}", opLog.getModule());
        } catch (Exception e) {
            log.error("发送操作日志到MQ失败，降级为同步写入, module={}", opLog.getModule(), e);
            // 降级：同步写入
            opLog.setWriteType(0);
            fallbackRecordOperationLog(opLog);
        }
    }

    /**
     * 记录工作流审批日志（异步）
     */
    public void recordWorkflowLog(String processInstanceId, String taskId, String taskName,
                                   String businessType, String businessKey,
                                   String action, String comment, String operator) {
        SysWorkflowLog wfLog = buildWorkflowLog(processInstanceId, taskId, taskName,
                businessType, businessKey, action, comment, operator);
        wfLog.setWriteType(1); // MQ 异步写入

        try {
            // 发送到 MQ
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.AUDIT_LOG_EXCHANGE,
                    RabbitMQConfig.WORKFLOW_LOG_ROUTING_KEY,
                    wfLog
            );
            log.info("工作流日志已发送到MQ, processInstanceId={}", processInstanceId);
        } catch (Exception e) {
            log.error("发送工作流日志到MQ失败，降级为同步写入, processInstanceId={}", processInstanceId, e);
            // 降级：同步写入
            wfLog.setWriteType(0);
            fallbackRecordWorkflowLog(wfLog);
        }
    }

    // ==================== 辅助方法 ====================

    private SysLoginLog buildLoginLog(String username, String loginType, String ip,
                                       int status, String message) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(username);
        loginLog.setLoginType(loginType);
        loginLog.setIp(ip);
        loginLog.setStatus(status);
        loginLog.setMessage(message);
        loginLog.setLoginTime(LocalDateTime.now());
        return loginLog;
    }

    private void fallbackRecordLoginLog(SysLoginLog loginLog) {
        try {
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("同步写入登录日志也失败", e);
        }
    }

    private void fallbackRecordOperationLog(SysOperationLog opLog) {
        try {
            operationLogMapper.insert(opLog);
        } catch (Exception e) {
            log.error("同步写入操作日志也失败", e);
        }
    }

    private void fallbackRecordWorkflowLog(SysWorkflowLog wfLog) {
        try {
            workflowLogMapper.insert(wfLog);
        } catch (Exception e) {
            log.error("同步写入工作流日志也失败", e);
        }
    }

    private SysWorkflowLog buildWorkflowLog(String processInstanceId, String taskId, String taskName,
                                             String businessType, String businessKey,
                                             String action, String comment, String operator) {
        SysWorkflowLog wfLog = new SysWorkflowLog();
        wfLog.setProcessInstanceId(processInstanceId);
        wfLog.setTaskId(taskId);
        wfLog.setTaskName(taskName);
        wfLog.setBusinessType(businessType);
        wfLog.setBusinessKey(businessKey);
        wfLog.setAction(action);
        wfLog.setComment(comment);
        wfLog.setOperator(operator);
        wfLog.setCreateTime(LocalDateTime.now());
        return wfLog;
    }
}
