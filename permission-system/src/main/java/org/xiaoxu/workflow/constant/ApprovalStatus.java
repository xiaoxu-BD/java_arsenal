package org.xiaoxu.workflow.constant;

import lombok.Getter;

/**
 * 审批状态枚举
 */
@Getter
public enum ApprovalStatus {

    DRAFT("草稿"),
    PROCESSING("审批中"),
    APPROVED("已通过"),
    REJECTED("已驳回"),
    CANCELLED("已撤回");

    private final String description;

    ApprovalStatus(String description) {
        this.description = description;
    }
}
