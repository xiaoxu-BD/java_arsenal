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
     */
    public static String getLabelByKey(String processDefinitionKey) {
        for (BusinessType type : values()) {
            if (type.getProcessDefinitionKey().equals(processDefinitionKey)) {
                return type.getLabel();
            }
        }
        return processDefinitionKey;
    }
}
