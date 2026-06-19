package org.xiaoxu.workflow.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 更新履约单 DTO
 */
@Data
public class FulfillmentOrderUpdateDTO implements Serializable {

    private Long id;

    private String title;

    private String description;

    private BigDecimal amount;

    private String remark;
}
