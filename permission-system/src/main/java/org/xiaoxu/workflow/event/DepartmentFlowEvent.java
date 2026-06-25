package org.xiaoxu.workflow.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 部门流程事件
 * 用于触发部门相关的业务逻辑
 */
@Getter
public class DepartmentFlowEvent extends ApplicationEvent {

    /**
     * 事件类型
     */
    public enum EventType {
        LEAVE_SUBMITTED,      // 请假已提交
        LEAVE_APPROVED,       // 请假已通过
        LEAVE_REJECTED,       // 请假已驳回
        FULFILLMENT_SUBMITTED, // 履约已提交
        FULFILLMENT_APPROVED,  // 履约已通过
        FULFILLMENT_REJECTED   // 履约已驳回
    }

    private final EventType eventType;
    private final String businessType;  // leave / fulfillment
    private final Long businessId;
    private final String applicant;
    private final String department;    // 部门
    private final String processInstanceId;

    public DepartmentFlowEvent(Object source, EventType eventType, String businessType,
                               Long businessId, String applicant, String department, String processInstanceId) {
        super(source);
        this.eventType = eventType;
        this.businessType = businessType;
        this.businessId = businessId;
        this.applicant = applicant;
        this.department = department;
        this.processInstanceId = processInstanceId;
    }
}
