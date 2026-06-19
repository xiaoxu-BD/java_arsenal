package org.xiaoxu.workflow.approval;

/**
 * 通用审批回调策略接口。每种接入 Flowable 审批的业务实现一个 Bean，
 * 由 {@link ApprovalHandlerRegistry} 按 processDefinitionKey 自动路由，
 * 在审批通过/驳回时回写各自的业务状态。
 *
 * <p>新增业务时只需实现本接口并注册为 Spring Bean，无需改动 FlowableController。
 */
public interface ApprovalHandler {

    /**
     * 该 Handler 处理的 Flowable 流程定义 key（如 fulfillment-approval、leave-request）。
     * 用作 {@link ApprovalHandlerRegistry} 的路由键。
     */
    String supportProcessDefinitionKey();

    /**
     * 流程全部审批通过（走到通过结束事件）时的回调。
     * 注意：仅当流程实例结束（最后一级审批通过）时才触发。
     */
    void onApproved(ApprovalContext context);

    /**
     * 驳回时的回调。驳回即结束流程，必定触发。
     */
    void onRejected(ApprovalContext context);

    /**
     * 由 businessKey 解析出业务单的主键 id，供前端待办列表跳转详情页使用。
     * 各业务 businessKey 格式不同：履约单为 orderNo（需查库反查 id），
     * 请假单为 "用户名:id"（直接解析）。查不到或无法解析时返回 null。
     */
    Long getBusinessId(String businessKey);
}
