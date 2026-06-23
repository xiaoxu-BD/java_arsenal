package org.xiaoxu.workflow.constant;

import lombok.Getter;

/**
 * 审批动作枚举
 */
@Getter
public enum ApprovalAction {

    APPROVE("通过"),
    REJECT("驳回");

    private final String label;

    ApprovalAction(String label) {
        this.label = label;
    }
}
