package org.xiaoxu.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
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
@Service
@RequiredArgsConstructor
public class FlowableLeaveServiceImpl implements FlowableLeaveService {

    private final RuntimeService runtimeService;
    private final ApproveLeaveService approveLeaveService;

    @Override
    public String startProcess(String processDefinitionKey, String businessKey, String days, Map<String, Object> variables) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put("days", days);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                processDefinitionKey, businessKey, variables);
        return processInstance.getId();
    }

    @Override
    public ApproveLeaveVO submitApproval(String userName, String identifier, String days) {
        LambdaQueryWrapper<ApproveLeave> wrapper = new LambdaQueryWrapper<ApproveLeave>()
                .eq(ApproveLeave::getIdentifier, identifier);
        ApproveLeave one = approveLeaveService.getOne(wrapper);
        if (Objects.isNull(one)) {
            throw new RuntimeException("请假单不存在");
        }

        String processId = startProcess("leave-request", userName + ":" + identifier, days, null);

        one.setProcessInstanceId(processId);
        one.setStatus(ApprovalStatus.PROCESSING.name());
        approveLeaveService.updateById(one);
        return BeanUtil.copyProperties(one, ApproveLeaveVO.class);
    }
}
