package org.xiaoxu.workflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.workflow.dto.ApproveLeaveCreateDTO;
import org.xiaoxu.workflow.service.ApproveLeaveService;
import org.xiaoxu.workflow.service.FlowableLeaveService;
import org.xiaoxu.workflow.vo.ApproveLeaveVO;

import java.util.List;

/**
 * 请假管理控制器
 */
@RestController
@RequestMapping("/api/application/leave")
@RequiredArgsConstructor
public class ApproveLeaveController {

    private final ApproveLeaveService approveLeaveService;
    private final FlowableLeaveService flowableLeaveService;

    /**
     * 创建请假单
     */
    @PostMapping
    public Result<ApproveLeaveVO> create(@Valid @RequestBody ApproveLeaveCreateDTO dto,
                                          Authentication authentication) {
        return Result.success(approveLeaveService.createLeave(dto, authentication.getName()));
    }

    /**
     * 我的请假列表
     */
    @GetMapping("/my")
    public Result<List<ApproveLeaveVO>> myLeaves(Authentication authentication) {
        return Result.success(approveLeaveService.getMyLeaves(authentication.getName()));
    }

    /**
     * 请假单详情
     */
    @GetMapping("/{id}")
    public Result<ApproveLeaveVO> detail(@PathVariable Long id) {
        return Result.success(approveLeaveService.getDetail(id));
    }

    /**
     * 提交审批
     * 默认走 v2 流程（LeaveApprovalHandler 只注册了 v2 的回调，v1 提交后业务状态将无人回写）
     */
    @PostMapping("/{id}/submit")
    public Result<ApproveLeaveVO> submitApproval(@PathVariable Long id,
                                                  @RequestParam String days,
                                                  @RequestParam String identifier,
                                                  @RequestParam(required = false) String processKey,
                                                  Authentication authentication) {
        String processDefinitionKey = (processKey != null && !processKey.isEmpty())
            ? processKey : org.xiaoxu.workflow.constant.ProcessDefinitionKey.LEAVE_REQUEST_V2;
        return Result.success(flowableLeaveService.submitApproval(
            authentication.getName(), identifier, days, processDefinitionKey));
    }

    /**
     * 删除请假单（仅草稿/已驳回状态，审批中的删除会导致流程实例与业务数据脱钩）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        org.xiaoxu.workflow.entity.ApproveLeave leave = approveLeaveService.getById(id);
        if (leave == null) {
            return Result.success();
        }
        String status = leave.getStatus();
        if (org.xiaoxu.workflow.constant.ApprovalStatus.PROCESSING.name().equals(status)) {
            throw new RuntimeException("审批中的请假单不允许删除");
        }
        approveLeaveService.removeById(id);
        return Result.success();
    }
}
