package org.xiaoxu.workflow.constant;

/**
 * 流程定义 Key 常量
 */
public final class ProcessDefinitionKey {

    private ProcessDefinitionKey() {}

    /** 请假审批流程（候选组模式） */
    public static final String LEAVE_REQUEST = "leave-request";

    /** 请假审批流程V2（流程变量指定审批人） */
    public static final String LEAVE_REQUEST_V2 = "leave-request-v2";

    /** 履约审批流程 */
    public static final String FULFILLMENT_APPROVAL = "fulfillment-approval";
}
