package org.xiaoxu.workflow.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 履约单状态变更 DTO
 */
@Data
public class FulfillmentStatusChangeDTO implements Serializable {

    private String status;
    private String remark;
}
