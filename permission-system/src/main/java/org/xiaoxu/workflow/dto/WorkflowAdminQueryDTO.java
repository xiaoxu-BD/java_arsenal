package org.xiaoxu.workflow.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 全部任务（管理员视角）分页查询 DTO。
 * 跨业务类型（请假/履约）聚合查询。
 */
@Data
public class WorkflowAdminQueryDTO implements Serializable {

    /** 业务类型：leave / fulfillment，为空表示全部 */
    private String businessType;

    /** 状态：DRAFT / PROCESSING / APPROVED / REJECTED / CANCELLED，为空表示全部 */
    private String status;

    /** 申请人（模糊） */
    private String applicant;

    private Integer current = 1;
    private Integer size = 10;
}
