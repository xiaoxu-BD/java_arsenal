package org.xiaoxu.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.xiaoxu.enums.PayMethod;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotBlank(message = "订单号不能为空")
    private String orderId;

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    @NotNull(message = "支付方式不能为空")
    private PayMethod payMethod;

    private String description;

}