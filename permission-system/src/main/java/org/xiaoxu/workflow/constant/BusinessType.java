package org.xiaoxu.workflow.constant;

import lombok.Getter;

/**
 * 业务类型枚举（与流程定义 Key 对应）
 */
@Getter
public enum BusinessType {

    LEAVE(ProcessDefinitionKey.LEAVE_REQUEST, "请假"),
    FULFILLMENT(ProcessDefinitionKey.FULFILLMENT_APPROVAL, "履约");

    private final String processDefinitionKey;
    private final String label;

    BusinessType(String processDefinitionKey, String label) {
        this.processDefinitionKey = processDefinitionKey;
        this.label = label;
    }

    /**
     * 根据流程定义 Key 获取中文标签
     * 兼容三种入参：流程定义 key（leave-request / leave-request-v2 / fulfillment-approval）、
     * 管理端聚合查询使用的短 key（leave / fulfillment）
     */
    public static String getLabelByKey(String key) {
        if (key == null) {
            return null;
        }
        for (BusinessType type : values()) {
            if (type.getProcessDefinitionKey().equals(key)) {
                return type.getLabel();
            }
        }
        if (ProcessDefinitionKey.LEAVE_REQUEST_V2.equals(key) || "leave".equals(key)) {
            return LEAVE.getLabel();
        }
        if ("fulfillment".equals(key)) {
            return FULFILLMENT.getLabel();
        }
        return key;
    }
}
