package org.xiaoxu.workflow.dto;

import lombok.Data;

/**
 * 审批通过请求
 */
@Data
public class ApproveRequest {

    /**
     * 审批意见（可选）
     */
    private String comment;
}
