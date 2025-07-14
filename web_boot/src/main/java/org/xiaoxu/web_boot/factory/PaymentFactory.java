package org.xiaoxu.web_boot.factory;

import lombok.extern.slf4j.Slf4j;
import org.xiaoxu.web_boot.service.Payment;
import org.xiaoxu.web_boot.service.impl.AliPay;
import org.xiaoxu.web_boot.service.impl.WeChatPay;

/**
 * @className: PaymentFactory
 * @author: xiaoxu
 * @date: 2025/6/29 9:28
 * @Version: 1.0
 * @description:
 */
@Slf4j
public class PaymentFactory {
    public static Payment createPayment(String paymentType) {
        return switch (paymentType) {
            case "aliPay" -> new AliPay();
            case "weChatPay" -> new WeChatPay();
            default -> throw new IllegalArgumentException("Unknown payment type: " + paymentType);
        };


    }
}
