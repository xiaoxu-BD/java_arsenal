package org.xiaoxu.workflow.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * 部门流程事件监听器
 * 处理部门相关的业务逻辑，如部门统计、通知等
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentFlowEventListener {

    /**
     * 处理部门流程事件
     * fallbackExecution = true：发布方没有事务时也执行，避免事件被静默丢弃
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleDepartmentFlowEvent(DepartmentFlowEvent event) {
        log.info("收到部门流程事件: eventType={}, businessType={}, department={}",
                event.getEventType(), event.getBusinessType(), event.getDepartment());

        try {
            switch (event.getEventType()) {
                case LEAVE_SUBMITTED:
                    handleLeaveSubmitted(event);
                    break;
                case LEAVE_APPROVED:
                    handleLeaveApproved(event);
                    break;
                case LEAVE_REJECTED:
                    handleLeaveRejected(event);
                    break;
                case FULFILLMENT_SUBMITTED:
                    handleFulfillmentSubmitted(event);
                    break;
                case FULFILLMENT_APPROVED:
                    handleFulfillmentApproved(event);
                    break;
                case FULFILLMENT_REJECTED:
                    handleFulfillmentRejected(event);
                    break;
                default:
                    log.warn("未知的事件类型: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("处理部门流程事件异常: eventType={}", event.getEventType(), e);
        }
    }

    /**
     * 请假已提交 - 通知部门经理
     */
    private void handleLeaveSubmitted(DepartmentFlowEvent event) {
        log.info("请假已提交，通知部门经理: applicant={}, department={}", 
                event.getApplicant(), event.getDepartment());
        // TODO: 发送通知给部门经理
        // notificationService.notifyDepartmentManager(event.getDepartment(), event.getApplicant(), "有新的请假申请待审批");
    }

    /**
     * 请假已通过 - 更新部门考勤统计
     */
    private void handleLeaveApproved(DepartmentFlowEvent event) {
        log.info("请假已通过，更新部门考勤: applicant={}, department={}", 
                event.getApplicant(), event.getDepartment());
        // TODO: 更新部门考勤统计
        // attendanceService.updateDepartmentAttendance(event.getDepartment(), event.getApplicant());
    }

    /**
     * 请假已驳回 - 通知申请人
     */
    private void handleLeaveRejected(DepartmentFlowEvent event) {
        log.info("请假已驳回，通知申请人: applicant={}, department={}", 
                event.getApplicant(), event.getDepartment());
        // TODO: 发送通知给申请人
        // notificationService.notifyApplicant(event.getApplicant(), "您的请假申请已被驳回");
    }

    /**
     * 履约已提交 - 通知部门经理
     */
    private void handleFulfillmentSubmitted(DepartmentFlowEvent event) {
        log.info("履约已提交，通知部门经理: applicant={}, department={}", 
                event.getApplicant(), event.getDepartment());
        // TODO: 发送通知给部门经理
    }

    /**
     * 履约已通过 - 更新部门业绩统计
     */
    private void handleFulfillmentApproved(DepartmentFlowEvent event) {
        log.info("履约已通过，更新部门业绩: applicant={}, department={}", 
                event.getApplicant(), event.getDepartment());
        // TODO: 更新部门业绩统计
    }

    /**
     * 履约已驳回 - 通知申请人
     */
    private void handleFulfillmentRejected(DepartmentFlowEvent event) {
        log.info("履约已驳回，通知申请人: applicant={}, department={}", 
                event.getApplicant(), event.getDepartment());
        // TODO: 发送通知给申请人
    }
}
