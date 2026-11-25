package org.xiaoxu.wecommail.payment.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户支付订单。状态机：WAIT_PAY → PAID / CLOSED。
 */
@Data
public class PaymentOrder {

    public static final String STATUS_WAIT_PAY = "WAIT_PAY";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CLOSED = "CLOSED";

    private String orderNo;
    /** 幂等号，数据库唯一约束兜底重复下单 */
    private String requestId;
    private Long userId;
    private String productCode;
    private String subject;
    private BigDecimal amount;
    private String status;
    /** 渠道交易号，支付成功后回填 */
    private String tradeNo;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime paidAt;
}
