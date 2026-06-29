package org.xiaoxu.pay.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 取消订单请求
 */
@Data
public class CancelOrderRequest {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /** 取消原因 */
    private String reason;
}
