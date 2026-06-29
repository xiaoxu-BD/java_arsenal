package org.xiaoxu.pay.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建订单请求
 */
@Data
public class CreateOrderRequest {

    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    @NotNull(message = "业务ID不能为空")
    private Long businessId;

    @NotBlank(message = "商品名称不能为空")
    private String productName;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额最小0.01")
    private BigDecimal amount;
}
