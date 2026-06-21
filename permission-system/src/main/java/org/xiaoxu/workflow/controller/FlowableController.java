package org.xiaoxu.workflow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.workflow.service.FlowableService;
import org.xiaoxu.workflow.service.FulfillmentOrderService;

import java.util.List;
import java.util.Map;

/**
 * Flowable 审批任务控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class FlowableController {

    private final FlowableService flowableService;
    private final FulfillmentOrderService fulfillmentOrderService;

    /**
     * 提交审批 - 将草稿状态的履约单提交审批，启动 Flowable 流程
     * @param processKey 可选，指定流程定义 key；不传则使用默认 "fulfillment-approval"
     */
    @PostMapping("/submit/{orderId}")
    public Result<?> submitApproval(@PathVariable Long orderId,
                                    @RequestParam(required = false) String processKey,
                                    Authentication authentication) {
        return Result.success(fulfillmentOrderService.submitApproval(orderId, authentication.getName(), processKey));
    }

    /**
     * 我的待办 - 已认领 ∪ 我所在 RBAC 角色为候选组的未认领任务
     */
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> myTasks(Authentication authentication) {
        return Result.success(flowableService.queryMyTasks(authentication.getName()));
    }

    /**
     * 候选任务 - 查询候选组的待办任务（未认领）
     */
    @GetMapping("/candidate")
    public Result<List<Map<String, Object>>> candidateTasks(@RequestParam String group) {
        return Result.success(flowableService.queryCandidateTasks(group));
    }

    /**
     * 认领任务 - 认领候选组中的任务
     */
    @PostMapping("/{taskId}/claim")
    public Result<Void> claimTask(@PathVariable String taskId, Authentication authentication) {
        flowableService.claimTask(taskId, authentication.getName());
        return Result.success();
    }

    /**
     * 审批通过 - 完成任务并标记为通过；事务边界与回调注册都在 service 内
     */
    @PostMapping("/{taskId}/approve")
    public Result<Void> approve(@PathVariable String taskId,
                                 @RequestBody(required = false) Map<String, String> body,
                                 Authentication authentication) {
        String comment = body == null ? null : body.get("comment");
        flowableService.completeAndCallback(taskId, authentication.getName(), true, comment);
        return Result.success();
    }

    /**
     * 审批驳回 - 完成任务并标记为驳回；事务边界与回调注册都在 service 内
     */
    @PostMapping("/{taskId}/reject")
    public Result<Void> reject(@PathVariable String taskId,
                                @RequestBody(required = false) Map<String, String> body,
                                Authentication authentication) {
        String comment = body == null ? null : body.get("comment");
        flowableService.completeAndCallback(taskId, authentication.getName(), false, comment);
        return Result.success();
    }

    /**
     * 流程历史 - 查询流程实例的审批历史记录（含审批意见）
     */
    @GetMapping("/history/{processInstanceId}")
    public Result<List<Map<String, Object>>> history(@PathVariable String processInstanceId) {
        return Result.success(flowableService.getHistoricActivities(processInstanceId));
    }

    /**
     * 流程图高亮数据 - 获取流程实例的高亮节点、连线和BPMN XML
     */
    @GetMapping("/process-diagram/{processInstanceId}")
    public Result<?> processDiagram(@PathVariable String processInstanceId) {
        return Result.success(flowableService.getProcessDiagramInfo(processInstanceId));
    }

    /**
     * 删除流程实例（管理员用，用于清理异常流程）
     * @param processInstanceId 流程实例ID
     * @param reason 删除原因
     */
    @DeleteMapping("/process/{processInstanceId}")
    public Result<Void> deleteProcess(@PathVariable String processInstanceId,
                                      @RequestParam(defaultValue = "管理员手动删除") String reason) {
        flowableService.deleteProcessInstance(processInstanceId, reason);
        return Result.success();
    }
}
