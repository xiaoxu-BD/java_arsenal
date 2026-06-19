package org.xiaoxu.workflow.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 全部任务列表统一行 —— 把请假/履约两张业务表抽象成统一的"工作流条目"。
 * <p>
 * 后端 SQL UNION ALL 出来；前端一份表格展示，详情按 businessType 跳转对应业务详情。
 */
@Data
public class WorkflowItemVO implements Serializable {

    /** 业务类型：leave / fulfillment */
    private String businessType;

    /** 业务主键（approve_leave.id / fulfillment_order.id） */
    private Long businessId;

    /** 业务唯一标识：leave=identifier(UUID)；fulfillment=order_no */
    private String businessKey;

    /** 标题：leave=leave_reason；fulfillment=title */
    private String title;

    /** 申请人：leave=user_name；fulfillment=applicant */
    private String applicant;

    /** 状态：DRAFT/PROCESSING/APPROVED/REJECTED/CANCELLED */
    private String status;

    /** 流程实例 ID（两表字段名不一致：process_instance_id / process_inst_id，SQL 里 alias 统一） */
    private String processInstanceId;

    /** 履约金额（仅 fulfillment 有值） */
    private BigDecimal amount;

    /** 请假天数（仅 leave 有值） */
    private BigDecimal leaveDay;

    private Date createTime;
    private Date updateTime;
}
