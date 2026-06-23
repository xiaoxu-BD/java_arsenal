package org.xiaoxu.workflow.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 待办任务 VO —— 用于"我的待办"列表，携带业务摘要信息
 */
@Data
public class TaskVO implements Serializable {

    /** Flowable 任务 ID */
    private String taskId;

    /** BPMN 节点名称（如"经理审批"） */
    private String taskName;

    /** 流程实例 ID */
    private String processInstanceId;

    /** 流程定义 Key（leave-request / fulfillment-approval） */
    private String processDefinitionKey;

    /** 业务 Key（leave=identifier；fulfillment=orderNo） */
    private String businessKey;

    /** 业务主键 ID（用于跳转详情） */
    private String businessId;

    /** 业务类型中文标签（请假 / 履约） */
    private String businessTypeLabel;

    /** 申请人 */
    private String applicant;

    /** 业务标题 / 请假原因 */
    private String title;

    /** 履约金额（仅履约有值） */
    private BigDecimal amount;

    /** 请假天数（仅请假有值） */
    private Integer days;

    /** 任务创建时间 */
    private LocalDateTime createTime;
}
