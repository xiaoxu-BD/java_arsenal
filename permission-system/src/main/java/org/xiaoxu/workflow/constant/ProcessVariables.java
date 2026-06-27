package org.xiaoxu.workflow.constant;

/**
 * 流程变量名常量类
 * 统一管理所有流程变量名字符串
 */
public final class ProcessVariables {

    private ProcessVariables() {}

    // ==================== 通用变量 ====================
    public static final String DAYS = "days";
    public static final String INITIATOR = "initiator";
    public static final String APPROVED = "approved";
    public static final String COMMENT = "comment";

    // ==================== 履约单变量 ====================
    public static final String AMOUNT = "amount";
    public static final String ORDER_NO = "orderNo";
    public static final String TITLE = "title";
    public static final String APPLICANT = "applicant";

    // ==================== 请假审批人变量 ====================
    public static final String MANAGER_APPROVER = "managerApprover";
    public static final String DIRECTOR_APPROVER = "directorApprover";
    public static final String HR_APPROVER = "hrApprover";
}
