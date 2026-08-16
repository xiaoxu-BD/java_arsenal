package org.xiaoxu.domain.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xiaoxu.domain.entity.PaymentOrder;
import org.xiaoxu.enums.PayMethod;
import org.xiaoxu.service.PayChannel;

// channel/AlipayChannel.java
@Slf4j
@Component
public class AlipayChannel implements PayChannel {

    @Override
    public PayMethod supportMethod() {
        return PayMethod.ALIPAY;
    }

    @Override
    public ChannelResult pay(PaymentOrder order) {
        log.info("[支付宝] 开始处理订单: {}", order.getOrderId());

        simulateNetworkDelay(300, 1000);

        // 模拟 95% 成功
        if (Math.random() < 0.95) {
            String txnId = "ALI" + System.currentTimeMillis();
            log.info("[支付宝] 订单 {} 成功, txnId={}", order.getOrderId(), txnId);
            return ChannelResult.ok(txnId);
        } else {
            log.warn("[支付宝] 订单 {} 失败: 系统繁忙", order.getOrderId());
            return ChannelResult.fail("系统繁忙，请稍后重试");
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