package org.xiaoxu.strategy;

import org.springframework.stereotype.Component;
import org.xiaoxu.service.PayService;

/**
 * @className: AliPayStrategy
 * @author: xiaoxu
 * @date: 2025/8/7 7:55
 * @Version: 1.0
 * @description:
 */
@Component
public class AliPayStrategy implements PayStrategy {
    @Override
    public String getType() {
        return "alipay";
    }

    @Override
    public void pay(double amount) {
        System.out.println("使用【支付宝】支付：" + amount);
    }
}
