package org.xiaoxu.domain.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xiaoxu.domain.entity.PaymentOrder;
import org.xiaoxu.enums.PayMethod;
import org.xiaoxu.service.PayChannel;

// channel/WechatPayChannel.java
@Slf4j
@Component
public class WechatPayChannel implements PayChannel {

    @Override
    public PayMethod supportMethod() {
        return PayMethod.WECHAT_PAY;
    }

    @Override
    public ChannelResult pay(PaymentOrder order) {
        log.info("[微信支付] 开始处理订单: {}", order.getOrderId());

        simulateNetworkDelay(500, 1500);

        // 模拟 90% 成功
        if (Math.random() < 0.9) {
            String txnId = "WX" + System.currentTimeMillis();
            log.info("[微信支付] 订单 {} 成功, txnId={}", order.getOrderId(), txnId);
            return ChannelResult.ok(txnId);
        } else {
            log.warn("[微信支付] 订单 {} 失败: 用户支付超时", order.getOrderId());
            return ChannelResult.fail("用户支付超时");
        }
    }

    private void simulateNetworkDelay(int minMs, int maxMs) {
        try {
            int delay = minMs + (int) (Math.random() * (maxMs - minMs));
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}