package org.xiaoxu.patterns.advanced.facade;

import java.math.BigDecimal;

/** 子系统：支付服务 */
public class PaymentService {

    public void charge(String orderNo, BigDecimal amount) {
        System.out.println("[支付] 单号=" + orderNo + " 扣款 " + amount + " 元");
    }
}
