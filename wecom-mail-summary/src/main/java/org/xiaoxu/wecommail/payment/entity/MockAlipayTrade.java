package org.xiaoxu.wecommail.payment.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模拟支付宝侧的渠道交易，与商户订单表隔离，演示“渠道已收款、商户未收到通知”的中间态。
 */
@Data
public class MockAlipayTrade {

    public static final String STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
    public static final String STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";
    public static final String STATUS_TRADE_CLOSED = "TRADE_CLOSED";

    private String orderNo;
    private String tradeNo;
    private BigDecimal amount;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime paidAt;
    /** 商户是否已应答 success，FALSE 时由通知任务重试 */
    private Boolean notifyAck;
}
