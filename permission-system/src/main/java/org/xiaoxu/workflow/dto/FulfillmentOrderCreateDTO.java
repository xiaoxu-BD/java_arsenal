package org.xiaoxu.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建履约单 DTO
 */
@Data
public class FulfillmentOrderCreateDTO implements Serializable {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private String remark;
}
