package org.xiaoxu.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.workflow.constant.ApprovalStatus;
import org.xiaoxu.workflow.entity.ApproveLeave;
import org.xiaoxu.workflow.service.ApproveLeaveService;
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

    private final RuntimeService runtimeService;
    private final ApproveLeaveService approveLeaveService;
    private final UserMapper userMapper;

    @Override
    public String startProcess(String processDefinitionKey, String businessKey, String days, Map<String, Object> variables) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        // 把 days 转换为 Double 类型，避免 Flowable 使用 Long.valueOf() 转换小数时报错
        try {
            variables.put("days", Double.parseDouble(days));
        } catch (NumberFormatException e) {
            variables.put("days", 0.0);
        }

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                processDefinitionKey, businessKey, variables);
        return processInstance.getId();
    }

    @Override
    public ApproveLeaveVO submitApproval(String userName, String identifier, String days, String processDefinitionKey) {
        LambdaQueryWrapper<ApproveLeave> wrapper = new LambdaQueryWrapper<ApproveLeave>()
                .eq(ApproveLeave::getIdentifier, identifier);
        ApproveLeave one = approveLeaveService.getOne(wrapper);
        if (Objects.isNull(one)) {
            throw new RuntimeException("请假单不存在");
        }

        // 构建流程变量
        Map<String, Object> variables = new HashMap<>();

        // 如果是 v2 流程（流程变量指定审批人），需要计算审批人
        if ("leave-request-v2".equals(processDefinitionKey)) {
            String managerApprover = findManagerApprover(userName);
            String directorApprover = findDirectorApprover(userName);
            String hrApprover = findHrApprover(userName);
            variables.put("managerApprover", managerApprover);
            variables.put("directorApprover", directorApprover);
            variables.put("hrApprover", hrApprover);
            log.info("使用V2流程，审批人变量：manager={}, director={}, hr={}", managerApprover, directorApprover, hrApprover);
        }

        String processId = startProcess(processDefinitionKey, userName + ":" + identifier, days, variables);

        one.setProcessInstanceId(processId);
        one.setStatus(ApprovalStatus.PROCESSING.name());
        approveLeaveService.updateById(one);
        return BeanUtil.copyProperties(one, ApproveLeaveVO.class);
    }

    /**
     * 查找经理审批人（示例：查询用户的直接上级）
     * 实际项目中应该查询组织架构表或审批配置表
     */
    private String findManagerApprover(String applicantUsername) {
        // 方案1：查询组织架构表找直接上级
        // return orgService.findDirectManager(applicantUsername);
        
        // 方案2：查询审批配置表
        // return approvalConfigService.getApprover("leave-request", "manager", applicantUsername);
        
        // 方案3：简单实现 - 查询角色为 ROLE_MANAGER 的用户
        try {
            SystemUsers manager = userMapper.selectOne(
                new LambdaQueryWrapper<SystemUsers>()
                    .eq(SystemUsers::getUsername, "manger_01") // 示例：固定返回 manager 用户
            );
            return manager != null ? manager.getUsername() : "manager";
        } catch (Exception e) {
            log.warn("查找经理审批人失败，使用默认值", e);
            return "manager";
        }
    }

    /**
     * 查找总监审批人
     */
    private String findDirectorApprover(String applicantUsername) {
        try {
            SystemUsers director = userMapper.selectOne(
                new LambdaQueryWrapper<SystemUsers>()
                    .eq(SystemUsers::getUsername, "director_01") // 示例：固定返回 director 用户
            );
            return director != null ? director.getUsername() : "director";
        } catch (Exception e) {
            log.warn("查找总监审批人失败，使用默认值", e);
            return "director";
        }
    }

    /**
     * 查找HR审批人
     */
    private String findHrApprover(String applicantUsername) {
        try {
            SystemUsers hr = userMapper.selectOne(
                new LambdaQueryWrapper<SystemUsers>()
                    .eq(SystemUsers::getUsername, "hr_01") // 示例：固定返回 hr 用户
            );
            return hr != null ? hr.getUsername() : "hr";
        } catch (Exception e) {
            log.warn("查找HR审批人失败，使用默认值", e);
            return "hr";
        }
    }
}
