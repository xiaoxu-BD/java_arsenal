package org.xiaoxu.workflow.approval;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.xiaoxu.workflow.constant.ApprovalStatus;
import org.xiaoxu.workflow.entity.ApproveLeave;
import org.xiaoxu.workflow.service.ApproveLeaveService;

/**
 * 请假单审批回调。流程定义 key 为 {@code leave-request}，
 * businessKey 格式为 {@code 用户名:identifier}（identifier 为 UUID 字符串，
 * 与 {@link ApproveLeave#getIdentifier()} 对应）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveApprovalHandler implements ApprovalHandler {

    private final ApproveLeaveService approveLeaveService;

    @Override
    public String supportProcessDefinitionKey() {
        return "leave-request-v2";
    }

    @Override
    public void onApproved(ApprovalContext context) {
        updateLeaveStatus(context.getBusinessKey(), ApprovalStatus.APPROVED);
    }

    @Override
    public void onRejected(ApprovalContext context) {
        updateLeaveStatus(context.getBusinessKey(), ApprovalStatus.REJECTED);
    }

    @Override
    public Long getBusinessId(String businessKey) {
        ApproveLeave leave = findByBusinessKey(businessKey);
        return leave == null ? null : leave.getId();
    }

    /**
     * 解析 businessKey（格式 "用户名:identifier"），按 identifier 反查请假单并更新状态。
     */
    private void updateLeaveStatus(String businessKey, ApprovalStatus status) {
        ApproveLeave leave = findByBusinessKey(businessKey);
        if (leave == null) {
            log.warn("请假单审批回调未找到记录, businessKey={}, 跳过状态更新", businessKey);
            return;
        }
        leave.setStatus(status.name());
        approveLeaveService.updateById(leave);
        log.info("请假单状态更新成功, id={}, identifier={}, status={}",
                leave.getId(), leave.getIdentifier(), status);
    }

    /**
     * 从 businessKey 拆出 identifier（UUID），按它反查请假单。
     */
    private ApproveLeave findByBusinessKey(String businessKey) {
        String identifier = parseIdentifier(businessKey);
        if (identifier == null) {
            return null;
        }
        return approveLeaveService.getOne(
                new LambdaQueryWrapper<ApproveLeave>()
                        .eq(ApproveLeave::getIdentifier, identifier));
    }

    private String parseIdentifier(String businessKey) {
        if (StringUtils.isBlank(businessKey)) {
            return null;
        }
        int idx = businessKey.lastIndexOf(':');
        if (idx < 0 || idx == businessKey.length() - 1) {
            return null;
        }
        return businessKey.substring(idx + 1);
    }
}
