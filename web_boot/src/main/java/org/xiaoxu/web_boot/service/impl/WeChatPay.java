package org.xiaoxu.web_boot.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.xiaoxu.web_boot.service.Payment;

/**
 * @className: WeChatPay
 * @author: xiaoxu
 * @date: 2025/6/29 9:27
 * @Version: 1.0
 * @description:
 */
@Slf4j
public class WeChatPay implements Payment {
    @Override
    public double calculatePayment(double amount) {
        if (amount > 0) {
            log.info("微信支付：{}",amount);
            return amount ;
        }
        return 0;
    }
}
