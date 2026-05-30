package org.xiaoxu.designpattern.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("wxPay")
public class WxPayStrategy implements PayStrategy {

    @Override
    public String channelCode() {
        return "WX";
    }

    @Override
    public String pay(String orderId, String amount) {
        log.info("【微信支付】orderId={}, amount={}", orderId, amount);
        // 调用微信支付SDK...
        return "WX_" + orderId + "_SUCCESS";
    }
}
