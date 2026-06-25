package org.xiaoxu.workflow.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.xiaoxu.workflow.approval.ApprovalContext;
import org.xiaoxu.workflow.approval.ApprovalHandler;
import org.xiaoxu.workflow.approval.ApprovalHandlerRegistry;
import org.xiaoxu.workflow.service.FlowableService;

/**
 * 审批事件监听器
 * 使用 @TransactionalEventListener 确保在事务提交后执行
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalEventListener {

    private final ApprovalHandlerRegistry approvalHandlerRegistry;
    private final FlowableService flowableService;

    /**
     * 处理审批事件
     * AFTER_COMMIT: 事务提交后执行，确保数据一致性
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalEvent(ApprovalEvent event) {
        log.info("收到审批事件: processInstanceId={}, action={}", 
                event.getProcessInstanceId(), event.getAction());

        try {
            ApprovalHandler handler = approvalHandlerRegistry.get(event.getProcessDefinitionKey());
            if (handler == null) {
                log.warn("未找到审批处理器: processDefinitionKey={}", event.getProcessDefinitionKey());
                return;
            }

            ApprovalContext ctx = ApprovalContext.builder()
                    .businessKey(event.getBusinessKey())
                    .processInstanceId(event.getProcessInstanceId())
                    .comment(event.getComment())
                    .build();

            if (event.getAction() == ApprovalEvent.ActionType.APPROVE) {
                // 整个流程通过才回调（仅最后一级通过时触发）
                if (flowableService.isProcessFinished(event.getProcessInstanceId())) {
                    handler.onApproved(ctx);
                    log.info("审批通过回调完成, processInstanceId={}", event.getProcessInstanceId());
                }
            } else {
                // 驳回即流程结束，回调一定触发
                handler.onRejected(ctx);
                log.info("审批驳回回调完成, processInstanceId={}", event.getProcessInstanceId());
            }
        } catch (Exception e) {
            log.error("审批回调异常, processInstanceId={}", event.getProcessInstanceId(), e);
        }
    }
}
