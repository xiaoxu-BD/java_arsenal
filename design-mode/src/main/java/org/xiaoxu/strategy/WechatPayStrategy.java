package org.xiaoxu.strategy;

import org.springframework.stereotype.Component;
import org.xiaoxu.service.PayService;

/**
 * @className: WechatPay
 * @author: xiaoxu
 * @date: 2025/8/7 7:54
 * @Version: 1.0
 * @description:
 */
@Component
public class WechatPayStrategy  implements PayStrategy {
    @Override
    public String getType() {
        return "Wechat";
    }

    @Override
    public void pay(double amount) {
        System.out.println("使用【微信】支付：" + amount);
    }
}
