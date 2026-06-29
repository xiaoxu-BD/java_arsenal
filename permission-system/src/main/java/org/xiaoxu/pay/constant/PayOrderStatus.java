package org.xiaoxu.pay.constant;

import lombok.Getter;

/**
 * 付款单状态枚举
 */
@Getter
public enum PayOrderStatus {

    PENDING("待支付"),
    PAID("已支付"),
    CANCELLED("已取消"),
    EXPIRED("已过期"),
    REFUNDED("已退款");

    private final String label;

    PayOrderStatus(String label) {
        this.label = label;
    }
}
