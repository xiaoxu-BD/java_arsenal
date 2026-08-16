package org.xiaoxu.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private String  orderId;
    private String  status;
    private String  transactionId;
    private String  message;
    private LocalDateTime payTime;

    public static PaymentResponse success(String orderId, String txnId) {
        return new PaymentResponse(
            orderId, "SUCCESS", txnId, "支付成功", LocalDateTime.now()
        );
    }

    public static PaymentResponse fail(String orderId, String msg) {
        return new PaymentResponse(
            orderId, "FAILED", null, msg, null
        );
    }
}