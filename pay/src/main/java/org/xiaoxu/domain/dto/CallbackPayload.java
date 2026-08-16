package org.xiaoxu.domain.dto;

import lombok.Data;

@Data
public class CallbackPayload {
    private String orderId;
    private String transactionId;
    private String tradeStatus; // TRADE_SUCCESS / TRADE_CLOSED
    private String sign;        // 签名（模拟用）
}