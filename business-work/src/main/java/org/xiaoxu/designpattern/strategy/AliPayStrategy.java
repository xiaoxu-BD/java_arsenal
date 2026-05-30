package org.xiaoxu.designpattern.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("aliPay")
public class AliPayStrategy implements PayStrategy {

    @Override
    public String channelCode() {
        return "ALI";
    }

    @Override
    public String pay(String orderId, String amount) {
        log.info("【支付宝支付】orderId={}, amount={}", orderId, amount);
        // 调用支付宝SDK...
        return "ALI_" + orderId + "_SUCCESS";
    }
}
