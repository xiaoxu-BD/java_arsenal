package org.xiaoxu.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.workflow.constant.ApprovalStatus;
import org.xiaoxu.workflow.constant.ProcessDefinitionKey;
import org.xiaoxu.workflow.constant.ProcessVariables;
import org.xiaoxu.workflow.entity.ApproveLeave;
import org.xiaoxu.workflow.entity.Department;
import org.xiaoxu.workflow.event.DepartmentFlowEvent;
import org.xiaoxu.workflow.service.ApproveLeaveService;
import org.xiaoxu.workflow.service.DepartmentService;
import org.xiaoxu.workflow.service.FlowableLeaveService;
import org.xiaoxu.workflow.vo.ApproveLeaveVO;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 请假流程服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowableLeaveServiceImpl implements FlowableLeaveService {

    private static final double DEFAULT_LEAVE_DAYS = 0.0;
    private static final String DEFAULT_MANAGER_APPROVER = "manger_01";
    private static final String DEFAULT_DIRECTOR_APPROVER = "director_01";
    private static final String DEFAULT_HR_APPROVER = "hr_01";
    private static final String UNKNOWN_DEPARTMENT = "UNKNOWN";

    private final RuntimeService runtimeService;
    private final ApproveLeaveService approveLeaveService;
    private final UserMapper userMapper;
    private final DepartmentService departmentService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public String startProcess(String processDefinitionKey, String businessKey, String days, Map<String, Object> variables) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        // 把 days 转换为 Double 类型，避免 Flowable 使用 Long.valueOf() 转换小数时报错
        if (days != null && !days.isEmpty()) {
            try {
                variables.put(ProcessVariables.DAYS, Double.parseDouble(days));
            } catch (NumberFormatException e) {
                log.warn("days 格式错误，使用默认值 {}: days={}", DEFAULT_LEAVE_DAYS, days);
                variables.put(ProcessVariables.DAYS, DEFAULT_LEAVE_DAYS);
            }
        } else {
            log.warn("days 为空，使用默认值 {}", DEFAULT_LEAVE_DAYS);
            variables.put(ProcessVariables.DAYS, DEFAULT_LEAVE_DAYS);
        }

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                processDefinitionKey, businessKey, variables);
        return processInstance.getId();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ApproveLeaveVO submitApproval(String userName, String identifier, String days, String processDefinitionKey) {
        LambdaQueryWrapper<ApproveLeave> wrapper = new LambdaQueryWrapper<ApproveLeave>()
                .eq(ApproveLeave::getIdentifier, identifier);
        ApproveLeave one = approveLeaveService.getOne(wrapper);
        if (Objects.isNull(one)) {
            throw new RuntimeException("请假单不存在");
        }

        // 只有草稿/已驳回的请假单才能提交，防止重复提交产生多个流程实例
        if (ApprovalStatus.PROCESSING.name().equals(one.getStatus())) {
            throw new RuntimeException("请假单已在审批中，请勿重复提交");
        }
        if (!ApprovalStatus.DRAFT.name().equals(one.getStatus())
                && !ApprovalStatus.REJECTED.name().equals(one.getStatus())) {
            throw new RuntimeException("当前状态不允许提交审批: " + one.getStatus());
        }

        // 审批层级所依赖的 days 以数据库存储的请假天数为准确认，客户端传值仅作兼容
        String effectiveDays = one.getLeaveDay() != null
                ? one.getLeaveDay().toPlainString() : days;

        // 构建流程变量
        Map<String, Object> variables = new HashMap<>();

        // 如果是 v2 流程（流程变量指定审批人），需要计算审批人
        if (ProcessDefinitionKey.LEAVE_REQUEST_V2.equals(processDefinitionKey)) {
            String managerApprover = findManagerApprover(userName);
            String directorApprover = findDirectorApprover(userName);
            String hrApprover = findHrApprover(userName);
            variables.put(ProcessVariables.MANAGER_APPROVER, managerApprover);
            variables.put(ProcessVariables.DIRECTOR_APPROVER, directorApprover);
            variables.put(ProcessVariables.HR_APPROVER, hrApprover);
            log.info("使用V2流程，审批人变量：manager={}, director={}, hr={}", managerApprover, directorApprover, hrApprover);
        }

        String processId = startProcess(processDefinitionKey, userName + ":" + identifier, effectiveDays, variables);

        // 条件更新抢占状态：并发提交时只有一个请求能成功，失败方随事务回滚（流程实例一并回滚）
        ApproveLeave patch = new ApproveLeave();
        patch.setStatus(ApprovalStatus.PROCESSING.name());
        patch.setProcessInstanceId(processId);
        boolean claimed = approveLeaveService.update(patch,
                new LambdaQueryWrapper<ApproveLeave>()
                        .eq(ApproveLeave::getId, one.getId())
                        .in(ApproveLeave::getStatus,
                                ApprovalStatus.DRAFT.name(), ApprovalStatus.REJECTED.name()));
        if (!claimed) {
            throw new RuntimeException("请假单已在审批中，请勿重复提交");
        }
        one.setStatus(ApprovalStatus.PROCESSING.name());
        one.setProcessInstanceId(processId);

        // 发布部门流程事件（请假已提交）
        String department = getUserDepartment(userName);
        DepartmentFlowEvent event = new DepartmentFlowEvent(
                this,
                DepartmentFlowEvent.EventType.LEAVE_SUBMITTED,
                "leave",
                one.getId(),
                userName,
                department,
                processId
        );
        eventPublisher.publishEvent(event);
        log.info("部门流程事件已发布: eventType=LEAVE_SUBMITTED, department={}", department);

        return BeanUtil.copyProperties(one, ApproveLeaveVO.class);
    }

    /**
     * 查找经理审批人
     */
    private String findManagerApprover(String applicantUsername) {
        try {
            SystemUsers manager = userMapper.selectOne(
                new LambdaQueryWrapper<SystemUsers>()
                    .eq(SystemUsers::getUsername, DEFAULT_MANAGER_APPROVER)
            );
            return manager != null ? manager.getUsername() : DEFAULT_MANAGER_APPROVER;
        } catch (Exception e) {
            log.warn("查找经理审批人失败，使用默认值", e);
            return DEFAULT_MANAGER_APPROVER;
        }
    }

    /**
     * 查找总监审批人
     */
    private String findDirectorApprover(String applicantUsername) {
        try {
            SystemUsers director = userMapper.selectOne(
                new LambdaQueryWrapper<SystemUsers>()
                    .eq(SystemUsers::getUsername, DEFAULT_DIRECTOR_APPROVER)
            );
            return director != null ? director.getUsername() : DEFAULT_DIRECTOR_APPROVER;
        } catch (Exception e) {
            log.warn("查找总监审批人失败，使用默认值", e);
            return DEFAULT_DIRECTOR_APPROVER;
        }
    }

    /**
     * 查找HR审批人
     */
    private String findHrApprover(String applicantUsername) {
        try {
            SystemUsers hr = userMapper.selectOne(
                new LambdaQueryWrapper<SystemUsers>()
                    .eq(SystemUsers::getUsername, DEFAULT_HR_APPROVER)
            );
            return hr != null ? hr.getUsername() : DEFAULT_HR_APPROVER;
        } catch (Exception e) {
            log.warn("查找HR审批人失败，使用默认值", e);
            return DEFAULT_HR_APPROVER;
        }
    }

    /**
     * 获取用户部门
     */
    private String getUserDepartment(String username) {
        try {
            Department dept = departmentService.getPrimaryDepartment(username);
            return dept != null ? dept.getDeptCode() : UNKNOWN_DEPARTMENT;
        } catch (Exception e) {
            log.warn("获取用户部门失败: username={}", username, e);
            return UNKNOWN_DEPARTMENT;
        }
    }
}
