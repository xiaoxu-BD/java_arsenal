package org.xiaoxu.workflow.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.stereotype.Component;
import org.xiaoxu.workflow.constant.ApprovalStatus;
import org.xiaoxu.workflow.entity.ApproveLeave;
import org.xiaoxu.workflow.entity.FulfillmentOrder;
import org.xiaoxu.workflow.service.ApproveLeaveService;
import org.xiaoxu.workflow.service.FulfillmentOrderService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批状态兜底定时任务
 * 用于处理流程已结束但业务状态未更新的情况
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalStatusSyncJob {

    private final HistoryService historyService;
    private final ApproveLeaveService approveLeaveService;
    private final FulfillmentOrderService fulfillmentOrderService;

    /**
     * 同步请假单审批状态
     * 每5分钟执行一次，检查流程已结束但状态仍为 PROCESSING 的请假单
     */
    @XxlJob("syncLeaveApprovalStatus")
    public void syncLeaveApprovalStatus() {
        log.info("开始同步请假单审批状态...");

        // 查询所有状态为 PROCESSING 的请假单
        List<ApproveLeave> leaveList = approveLeaveService.list(
                new LambdaQueryWrapper<ApproveLeave>()
                        .eq(ApproveLeave::getStatus, ApprovalStatus.PROCESSING.name())
                        .isNotNull(ApproveLeave::getProcessInstanceId)
        );

        if (leaveList.isEmpty()) {
            log.info("没有需要同步的请假单");
            return;
        }

        int updatedCount = 0;
        for (ApproveLeave leave : leaveList) {
            try {
                String processInstanceId = leave.getProcessInstanceId();
                
                // 查询流程实例历史
                HistoricProcessInstance processInstance = historyService
                        .createHistoricProcessInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .singleResult();

                if (processInstance == null) {
                    log.warn("流程实例不存在, processInstanceId={}", processInstanceId);
                    continue;
                }

                // 流程已结束
                if (processInstance.getEndTime() != null) {
                    // 判断是通过还是驳回（通过结束时间判断）
                    // 这里简化处理：查询最后一个用户任务的审批动作
                    String newStatus = determineApprovalStatus(processInstanceId);
                    
                    if (newStatus != null && !newStatus.equals(leave.getStatus())) {
                        leave.setStatus(newStatus);
                        approveLeaveService.updateById(leave);
                        updatedCount++;
                        log.info("请假单状态已同步, id={}, identifier={}, newStatus={}",
                                leave.getId(), leave.getIdentifier(), newStatus);
                    }
                }
            } catch (Exception e) {
                log.error("同步请假单状态失败, id={}", leave.getId(), e);
            }
        }

        log.info("请假单状态同步完成, 共处理{}条, 更新{}条", leaveList.size(), updatedCount);
    }

    /**
     * 同步履约单审批状态
     * 每5分钟执行一次
     */
    @XxlJob("syncFulfillmentApprovalStatus")
    public void syncFulfillmentApprovalStatus() {
        log.info("开始同步履约单审批状态...");

        List<FulfillmentOrder> orderList = fulfillmentOrderService.list(
                new LambdaQueryWrapper<FulfillmentOrder>()
                        .eq(FulfillmentOrder::getStatus, ApprovalStatus.PROCESSING.name())
                        .isNotNull(FulfillmentOrder::getProcessInstId)
        );

        if (orderList.isEmpty()) {
            log.info("没有需要同步的履约单");
            return;
        }

        int updatedCount = 0;
        for (FulfillmentOrder order : orderList) {
            try {
                String processInstanceId = order.getProcessInstId();
                
                HistoricProcessInstance processInstance = historyService
                        .createHistoricProcessInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .singleResult();

                if (processInstance == null) {
                    log.warn("流程实例不存在, processInstanceId={}", processInstanceId);
                    continue;
                }

                if (processInstance.getEndTime() != null) {
                    String newStatus = determineApprovalStatus(processInstanceId);
                    
                    if (newStatus != null && !newStatus.equals(order.getStatus())) {
                        order.setStatus(newStatus);
                        fulfillmentOrderService.updateById(order);
                        updatedCount++;
                        log.info("履约单状态已同步, id={}, orderNo={}, newStatus={}",
                                order.getId(), order.getOrderNo(), newStatus);
                    }
                }
            } catch (Exception e) {
                log.error("同步履约单状态失败, id={}", order.getId(), e);
            }
        }

        log.info("履约单状态同步完成, 共处理{}条, 更新{}条", orderList.size(), updatedCount);
    }

    /**
     * 根据流程实例判断审批结果
     * 通过查询流程变量中的 approved 字段判断
     */
    private String determineApprovalStatus(String processInstanceId) {
        try {
            // 查询流程变量
            Map<String, Object> variables = new HashMap<>();
            historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list()
                    .forEach(var -> variables.put(var.getVariableName(), var.getValue()));

            // 判断审批结果
            Object approved = variables.get("approved");
            if (approved instanceof Boolean) {
                return Boolean.TRUE.equals(approved) 
                        ? ApprovalStatus.APPROVED.name() 
                        : ApprovalStatus.REJECTED.name();
            }

            // 如果没有 approved 变量，可能是自动完成的（如HR备案）
            // 默认返回已通过
            return ApprovalStatus.APPROVED.name();
        } catch (Exception e) {
            log.error("判断审批结果失败, processInstanceId={}", processInstanceId, e);
            return null;
        }
    }
}
