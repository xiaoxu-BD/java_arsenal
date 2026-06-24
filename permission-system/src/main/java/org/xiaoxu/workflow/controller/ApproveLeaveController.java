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
     */
    @PostMapping("/{id}/submit")
    public Result<ApproveLeaveVO> submitApproval(@PathVariable Long id,
                                                  @RequestParam String days,
                                                  @RequestParam String identifier,
                                                  @RequestParam(required = false) String processKey,
                                                  Authentication authentication) {
        String processDefinitionKey = (processKey != null && !processKey.isEmpty()) 
            ? processKey : "leave-request";
        return Result.success(flowableLeaveService.submitApproval(
            authentication.getName(), identifier, days, processDefinitionKey));
    }

    /**
     * 删除请假单（仅草稿状态）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        approveLeaveService.removeById(id);
        return Result.success();
    }
}
