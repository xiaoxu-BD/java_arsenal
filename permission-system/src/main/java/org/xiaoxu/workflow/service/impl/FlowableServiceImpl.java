package org.xiaoxu.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.workflow.approval.ApprovalContext;
import org.xiaoxu.workflow.approval.ApprovalHandler;
import org.xiaoxu.workflow.approval.ApprovalHandlerRegistry;
import org.xiaoxu.workflow.constant.ApprovalAction;
import org.xiaoxu.workflow.constant.BusinessType;
import org.xiaoxu.workflow.constant.ProcessDefinitionKey;
import org.xiaoxu.workflow.entity.ApproveLeave;
import org.xiaoxu.workflow.entity.FulfillmentOrder;
import org.xiaoxu.workflow.identity.WorkflowIdentityService;
import org.xiaoxu.workflow.mapper.ApproveLeaveMapper;
import org.xiaoxu.workflow.mapper.FulfillmentOrderMapper;
import org.xiaoxu.workflow.service.FlowableService;
import org.xiaoxu.service.AuditLogService;
import org.xiaoxu.workflow.vo.ProcessDiagramVO;
import org.xiaoxu.workflow.vo.TaskVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Flowable 工作流核心服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowableServiceImpl implements FlowableService {

    /** BPMN 文件部署/删除/查询 */
    private final RepositoryService repositoryService;
    /** 管理"流程实例"（启动/暂停/删除运行中的流程） */
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final ApprovalHandlerRegistry approvalHandlerRegistry;
    private final WorkflowIdentityService workflowIdentityService;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;
    private final ApproveLeaveMapper approveLeaveMapper;
    private final FulfillmentOrderMapper fulfillmentOrderMapper;

    @Override
    public String deployProcess(String bpmnResourcePath) {
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource(bpmnResourcePath)
                .deploy();
        log.info("流程部署成功, deploymentId: {}", deployment.getId());
        return deployment.getId();
    }

    @Override
    public String startProcess(String processDefinitionKey, String businessKey,
                                String initiator, Map<String, Object> variables) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put("initiator", initiator);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                processDefinitionKey, businessKey, variables);
        log.info("流程启动成功, processInstanceId: {}", processInstance.getId());
        return processInstance.getId();
    }

    @Override
    public List<TaskVO> queryMyTasks(String assignee) {
        // 1) 已认领（assignee = 当前用户）
        List<Task> claimed = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .orderByTaskCreateTime().desc()
                .list();

        // 2) 我所在 RBAC 角色仍为候选组、且未被任何人认领的任务
        List<String> myGroups = workflowIdentityService.getGroupsOf(assignee);
        List<Task> candidate = myGroups.isEmpty()
                ? Collections.emptyList()
                : taskService.createTaskQuery()
                        .taskCandidateGroupIn(myGroups)
                        .taskUnassigned()
                        .orderByTaskCreateTime().desc()
                        .list();

        if (claimed.isEmpty() && candidate.isEmpty()) {
            return Collections.emptyList();
        }

        // 合并去重：按 taskId 保留首次出现（已认领优先）
        Map<String, Task> merged = new LinkedHashMap<>();
        Stream.concat(claimed.stream(), candidate.stream())
                .forEach(t -> merged.putIfAbsent(t.getId(), t));
        return convertToTaskVO(new ArrayList<>(merged.values()));
    }

    @Override
    public List<TaskVO> queryCandidateTasks(String candidateGroup) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateGroup(candidateGroup)
                .orderByTaskCreateTime().desc()
                .list();
        return convertToTaskVO(tasks);
    }

    @Override
    public void claimTask(String taskId, String userId) {
        taskService.claim(taskId, userId);
        log.info("用户 {} 认领任务 {}", userId, taskId);
    }

    @Override
    public boolean claimIfUnassigned(String taskId, String userId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        if (task.getAssignee() != null) {
            return false;
        }
        taskService.claim(taskId, userId);
        log.info("用户 {} 自动认领任务 {}", userId, taskId);
        return true;
    }

    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        if (variables != null) {
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }
        log.info("任务完成, taskId: {}", taskId);
    }

    @Override
    @Transactional
    public void completeAndCallback(String taskId, String username, boolean approved, String comment) {
        // 1) 取出路由信息（complete 之后流程实例可能已被删除）
        final String processDefinitionKey = getProcessDefinitionKeyByTaskId(taskId);
        final String businessKey = getBusinessKeyByTaskId(taskId);
        final String processInstanceId = getProcessInstanceIdByTaskId(taskId);

        // 2) 候选组任务 → 自动认领
        claimIfUnassigned(taskId, username);

        // 3) 写入 ACT_HI_COMMENT，持久化审批意见到流程历史
        if (StringUtils.isNotBlank(comment)) {
            // 在认证上下文里设置当前用户，addComment 会用作 USER_ID_
            org.flowable.common.engine.impl.identity.Authentication.setAuthenticatedUserId(username);
            try {
                taskService.addComment(taskId, processInstanceId, comment);
            } finally {
                org.flowable.common.engine.impl.identity.Authentication.setAuthenticatedUserId(null);
            }
        }

        // 4) 完成任务（同时把 approved/comment 作为流程变量）
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", approved);
        if (StringUtils.isNotBlank(comment)) {
            variables.put("comment", comment);
        }
        // 记录任务名称（完成前查询，完成后任务可能被删除）
        Task currentTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        String taskName = currentTask != null ? currentTask.getName() : taskId;
        completeTask(taskId, variables);

        // 4.5) 记录审批日志
        String action = approved ? ApprovalAction.APPROVE.name() : ApprovalAction.REJECT.name();
        auditLogService.recordWorkflowLog(
                processInstanceId, taskId, taskName,
                processDefinitionKey, businessKey,
                action, comment, username);

        // 5) 注册 afterCommit 回调：事务提交后再触发业务侧 onApproved/onRejected
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    ApprovalHandler handler = approvalHandlerRegistry.get(processDefinitionKey);
                    if (handler == null) {
                        return;
                    }
                    ApprovalContext ctx = ApprovalContext.builder()
                            .businessKey(businessKey)
                            .processInstanceId(processInstanceId)
                            .comment(comment)
                            .build();
                    if (approved) {
                        // 整个流程通过才回调（仅最后一级通过时触发）
                        if (isProcessFinished(processInstanceId)) {
                            handler.onApproved(ctx);
                            log.info("审批通过回调完成, processInstanceId={}, key={}", processInstanceId, processDefinitionKey);
                        }
                    } else {
                        // 驳回即流程结束，回调一定触发
                        handler.onRejected(ctx);
                        log.info("审批驳回回调完成, processInstanceId={}, key={}", processInstanceId, processDefinitionKey);
                    }
                } catch (Exception e) {
                    log.error("审批回调异常, processInstanceId={}", processInstanceId, e);
                }
            }
        });
    }

    @Override
    public List<Map<String, Object>> getHistoricActivities(String processInstanceId) {
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();

        // 一次性取该流程实例下所有 comments，按 taskId 分组（同一任务可能多条审批意见）
        Map<String, List<String>> commentsByTaskId = taskService
                .getProcessInstanceComments(processInstanceId)
                .stream()
                .filter(c -> c.getTaskId() != null)
                .collect(Collectors.groupingBy(
                        Comment::getTaskId,
                        Collectors.mapping(Comment::getFullMessage, Collectors.toList())));

        // 获取流程变量中的审批动作
        Map<String, String> actionByTaskId = new HashMap<>();
        try {
            historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .variableName("approved")
                    .list()
                    .forEach(var -> {
                        if (var.getTaskId() != null) {
                            Boolean approved = (Boolean) var.getValue();
                            actionByTaskId.put(var.getTaskId(), Boolean.TRUE.equals(approved) ? "APPROVE" : "REJECT");
                        }
                    });
        } catch (Exception e) {
            log.warn("获取审批动作变量失败", e);
        }

        // 收集所有审批人用户名，批量查询用户信息（头像、昵称）
        Set<String> assigneeNames = activities.stream()
                .map(HistoricActivityInstance::getAssignee)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, SystemUsers> userMap = new HashMap<>();
        if (!assigneeNames.isEmpty()) {
            try {
                List<SystemUsers> users = userMapper.selectList(new LambdaQueryWrapper<SystemUsers>().in(SystemUsers::getUsername,assigneeNames));
                userMap = users.stream()
                        .collect(Collectors.toMap(SystemUsers::getUsername, u -> u, (a, b) -> a));
            } catch (Exception e) {
                log.warn("批量查询用户信息失败", e);
            }
        }

        final Map<String, SystemUsers> finalUserMap = userMap;

        return activities.stream().map(activity -> {
            Map<String, Object> map = new HashMap<>();
            map.put("activityId", activity.getActivityId());
            map.put("activityName", activity.getActivityName());
            map.put("activityType", activity.getActivityType());
            map.put("startTime", activity.getStartTime());
            map.put("endTime", activity.getEndTime());
            map.put("assignee", activity.getAssignee());
            map.put("durationInMillis", activity.getDurationInMillis());

            // 用户头像和昵称
            String assignee = activity.getAssignee();
            if (StringUtils.isNotBlank(assignee) && finalUserMap.containsKey(assignee)) {
                SystemUsers user = finalUserMap.get(assignee);
                map.put("avatar", user.getAvatar());
                map.put("nickname", user.getNickname());
            }

            // 审批动作
            if (activity.getTaskId() != null) {
                map.put("action", actionByTaskId.getOrDefault(activity.getTaskId(), null));
            }
            // 把该任务节点下的审批意见列表附加到节点上
            if (activity.getTaskId() != null) {
                map.put("comments", commentsByTaskId.getOrDefault(activity.getTaskId(), Collections.emptyList()));
            } else {
                map.put("comments", Collections.emptyList());
            }
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public String getBusinessKeyByTaskId(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult()
                .getBusinessKey();
    }

    @Override
    public String getProcessInstanceIdByTaskId(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        return task.getProcessInstanceId();
    }

    @Override
    public String getProcessDefinitionKeyByTaskId(String taskId) {
        /* 等价 SQL：
  SELECT * FROM ACT_RU_TASK WHERE ID_ = #{taskId}
*/
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        //SELECT * FROM ACT_RE_PROCDEF WHERE ID_ = #{processDefinitionId}
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult();
        if (definition == null) {
            throw new RuntimeException("流程定义不存在: " + task.getProcessDefinitionId());
        }
        return definition.getKey();
    }

    @Override
    public boolean isProcessFinished(String processInstanceId) {
        // 流程结束后，运行时表里的流程实例会被删除，所以查不到 = 已结束
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        return processInstance == null;
    }

    @Override
    public ProcessDiagramVO getProcessDiagramInfo(String processInstanceId) {
        ProcessDiagramVO vo = new ProcessDiagramVO();
        vo.setProcessInstanceId(processInstanceId);

        // 获取流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();

        // 获取流程定义 ID
        String processDefinitionId;
        if (processInstance != null) {
            processDefinitionId = processInstance.getProcessDefinitionId();
        } else {
            // 流程已结束，从历史获取
            processDefinitionId = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult()
                    .getProcessDefinitionId();
        }
        vo.setProcessDefinitionId(processDefinitionId);

        // 获取已完成的节点和连线
        List<HistoricActivityInstance> finishedActivities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();

        List<String> finishedNodes = finishedActivities.stream()
                .map(HistoricActivityInstance::getActivityId)
                .distinct()
                .collect(Collectors.toList());
        vo.setFinishedNodes(finishedNodes);

        // 获取当前活跃节点
        List<String> activeNodes;
        if (processInstance != null) {
            activeNodes = runtimeService.createActivityInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .unfinished()
                    .list()
                    .stream()
                    .map(a -> a.getActivityId())
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            activeNodes = List.of();
        }
        vo.setActiveNodes(activeNodes);

        // 根据完成节点推断已走过的连线
        List<String> finishedFlows = new java.util.ArrayList<>();
        for (int i = 0; i < finishedNodes.size() - 1; i++) {
            finishedFlows.add(finishedNodes.get(i) + " -> " + finishedNodes.get(i + 1));
        }
        vo.setFinishedFlows(finishedFlows);

        // 获取 BPMN XML
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId).singleResult();
            java.io.InputStream bpmnStream = repositoryService.getResourceAsStream(
                    processDefinition.getDeploymentId(), processDefinition.getResourceName());
            vo.setBpmnXml(new String(bpmnStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            bpmnStream.close();
        } catch (Exception e) {
            log.warn("获取BPMN XML失败: {}", e.getMessage());
            vo.setBpmnXml(null);
        }

        return vo;
    }

    @Override
    public void deleteProcessInstance(String processInstanceId, String reason) {
        // 检查流程实例是否存在
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (processInstance != null) {
            // 删除运行中的流程实例（级联删除历史）
            runtimeService.deleteProcessInstance(processInstanceId, reason);
            log.info("删除流程实例成功, processInstanceId={}, reason={}", processInstanceId, reason);
        } else {
            // 流程已结束，只删除历史数据
            historyService.deleteHistoricProcessInstance(processInstanceId);
            log.info("删除历史流程实例成功, processInstanceId={}", processInstanceId);
        }
    }

    /**
     * 将 Task 列表转换为 TaskVO 列表，携带业务摘要信息
     */
    private List<TaskVO> convertToTaskVO(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return List.of();
        }

        // 批量查询流程实例，避免逐条 N+1
        Set<String> processInstanceIds = tasks.stream()
                .map(Task::getProcessInstanceId)
                .collect(Collectors.toSet());
        Map<String, ProcessInstance> instanceMap = runtimeService.createProcessInstanceQuery()
                .processInstanceIds(processInstanceIds)
                .list()
                .stream()
                .collect(Collectors.toMap(ProcessInstance::getProcessInstanceId, pi -> pi));

        return tasks.stream().map(task -> {
            TaskVO vo = new TaskVO();
            vo.setTaskId(task.getId());
            vo.setTaskName(task.getName());
            vo.setProcessInstanceId(task.getProcessInstanceId());
            vo.setCreateTime(task.getCreateTime() != null ? task.getCreateTime().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);

            ProcessInstance instance = instanceMap.get(task.getProcessInstanceId());
            if (instance != null) {
                String pdKey = instance.getProcessDefinitionKey();
                String businessKey = instance.getBusinessKey();
                vo.setProcessDefinitionKey(pdKey);
                vo.setBusinessKey(businessKey);
                vo.setBusinessTypeLabel(BusinessType.getLabelByKey(pdKey));

                // 通过审批回调解析业务主键 id
                Long businessId = approvalHandlerRegistry.getBusinessId(pdKey, businessKey);
                if (businessId != null) {
                    vo.setBusinessId(String.valueOf(businessId));
                }

                // 查询业务详情，填充摘要信息
                fillBusinessSummary(vo, pdKey, businessId);
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 根据业务类型查询并填充摘要信息（申请人、标题、金额/天数）
     */
    private void fillBusinessSummary(TaskVO vo, String processDefinitionKey, Long businessId) {
        if (businessId == null) {
            return;
        }
        try {
            if (ProcessDefinitionKey.FULFILLMENT_APPROVAL.equals(processDefinitionKey)) {
                FulfillmentOrder order = fulfillmentOrderMapper.selectById(businessId);
                if (order != null) {
                    vo.setApplicant(order.getApplicant());
                    vo.setTitle(order.getTitle());
                    vo.setAmount(order.getAmount());
                }
            } else if (ProcessDefinitionKey.LEAVE_REQUEST.equals(processDefinitionKey)) {
                ApproveLeave leave = approveLeaveMapper.selectById(businessId);
                if (leave != null) {
                    vo.setApplicant(leave.getUserName());
                    vo.setTitle(leave.getLeaveReason());
                    vo.setDays(leave.getLeaveDay() != null ? leave.getLeaveDay().intValue() : null);
                }
            }
        } catch (Exception e) {
            log.warn("查询业务摘要失败, businessId={}, key={}", businessId, processDefinitionKey, e);
        }
    }
}
