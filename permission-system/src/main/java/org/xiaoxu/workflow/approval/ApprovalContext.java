package org.xiaoxu.workflow.approval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批回调上下文，由 FlowableController 在审批通过/驳回时构造，
 * 传递给各业务的 {@link ApprovalHandler} 用于回写业务状态。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalContext {

    /** 流程业务键，履约单为 orderNo，请假单为 "用户名:id" */
    private String businessKey;

    /** 流程实例 ID */
    private String processInstanceId;

    /** 审批意见（可为空） */
    private String comment;
}
