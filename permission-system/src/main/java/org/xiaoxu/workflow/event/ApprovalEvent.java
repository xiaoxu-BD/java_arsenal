package org.xiaoxu.workflow.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 审批事件
 * 用于在事务提交后触发业务回调
 */
@Getter
public class ApprovalEvent extends ApplicationEvent {

    /**
     * 审批动作类型
     */
    public enum ActionType {
        APPROVE,  // 通过
        REJECT    // 驳回
    }

    private final String processInstanceId;
    private final String processDefinitionKey;
    private final String businessKey;
    private final ActionType action;
    private final String comment;
    private final String approver;

    public ApprovalEvent(Object source, String processInstanceId, String processDefinitionKey,
                         String businessKey, ActionType action, String comment, String approver) {
        super(source);
        this.processInstanceId = processInstanceId;
        this.processDefinitionKey = processDefinitionKey;
        this.businessKey = businessKey;
        this.action = action;
        this.comment = comment;
        this.approver = approver;
    }
}
