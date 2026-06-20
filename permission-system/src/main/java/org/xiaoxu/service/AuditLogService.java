package org.xiaoxu.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.xiaoxu.mapper.SysLoginLogMapper;
import org.xiaoxu.mapper.SysOperationLogMapper;
import org.xiaoxu.mapper.SysWorkflowLogMapper;
import org.xiaoxu.pojo.SysLoginLog;
import org.xiaoxu.pojo.SysOperationLog;
import org.xiaoxu.pojo.SysWorkflowLog;

import java.time.LocalDateTime;

/**
 * 审计日志服务 — 统一封装三张日志表的写入
 */
@Slf4j
@Service
public class AuditLogService {

    @Resource
    private SysLoginLogMapper loginLogMapper;

    @Resource
    private SysOperationLogMapper operationLogMapper;

    @Resource
    private SysWorkflowLogMapper workflowLogMapper;

    /**
     * 记录登录日志
     */
    public void recordLoginLog(String username, String loginType, String ip,
                               String browser, String os, int status, String message) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUsername(username);
            loginLog.setLoginType(loginType);
            loginLog.setIp(ip);
            loginLog.setBrowser(browser);
            loginLog.setOs(os);
            loginLog.setStatus(status);
            loginLog.setMessage(message);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
        }
    }

    /**
     * 记录业务操作日志
     */
    public void recordOperationLog(SysOperationLog opLog) {
        try {
            if (opLog.getCreateTime() == null) {
                opLog.setCreateTime(LocalDateTime.now());
            }
            operationLogMapper.insert(opLog);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    /**
     * 记录工作流审批日志
     */
    public void recordWorkflowLog(String processInstanceId, String taskId, String taskName,
                                   String businessType, String businessKey,
                                   String action, String comment, String operator) {
        try {
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
            workflowLogMapper.insert(wfLog);
        } catch (Exception e) {
            log.error("记录审批日志失败", e);
        }
    }
}
