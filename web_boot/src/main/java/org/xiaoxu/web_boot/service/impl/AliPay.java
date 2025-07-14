package org.xiaoxu.web_boot.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.xiaoxu.web_boot.service.Payment;

/**
 * @className: AliPay
 * @author: xiaoxu
 * @date: 2025/6/29 9:26
 * @Version: 1.0
 * @description:
 */
@Slf4j
public class AliPay implements Payment {
    @Override
    public double calculatePayment(double amount) {

        if (amount > 0) {
            log.info("支付宝支付：{}",amount);
            return amount * 0.9; // 支付宝支付有10%的折扣
        }

        return 0;
    }
}
