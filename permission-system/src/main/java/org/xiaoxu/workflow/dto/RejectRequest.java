package org.xiaoxu.workflow.dto;

import lombok.Data;

/**
 * 审批驳回请求
 */
@Data
public class RejectRequest {

    /**
     * 审批意见（可选）
     */
    private String comment;
}
