package org.xiaoxu.workflow.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 履约单分页查询 DTO
 */
@Data
public class FulfillmentOrderQueryDTO implements Serializable {

    private String orderNo;
    private String title;
    private String status;
    private String applicant;
    private Integer current = 1;
    private Integer size = 10;
}
