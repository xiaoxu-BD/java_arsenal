package org.xiaoxu.domain.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xiaoxu.domain.entity.PaymentOrder;
import org.xiaoxu.enums.PayMethod;
import org.xiaoxu.service.PayChannel;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// channel/BalancePayChannel.java
@Slf4j
@Component
public class BalancePayChannel implements PayChannel {

    /** 模拟用户余额表 */
    private static final Map<String, BigDecimal> USER_BALANCE = new ConcurrentHashMap<>();

    static {
        USER_BALANCE.put("user001", new BigDecimal("10000.00"));
        USER_BALANCE.put("user002", new BigDecimal("50.00"));
        USER_BALANCE.put("user003", new BigDecimal("999999.00"));
    }

    @Override
    public PayMethod supportMethod() {
        return PayMethod.BALANCE;
    }

    @Override
    public ChannelResult pay(PaymentOrder order) {
        log.info("[余额支付] 订单 {}, 用户 {}, 金额 {}",
                order.getOrderId(), order.getUserId(), order.getAmount());

        BigDecimal balance = USER_BALANCE.getOrDefault(
                order.getUserId(), BigDecimal.ZERO);

        // 余额不足
        if (balance.compareTo(order.getAmount()) < 0) {
            log.warn("[余额支付] 用户 {} 余额不足, 余额={}, 需付={}",
                    order.getUserId(), balance, order.getAmount());
            return ChannelResult.fail("余额不足，当前余额: " + balance);
        }

        // 扣减
        BigDecimal newBalance = balance.subtract(order.getAmount());
        USER_BALANCE.put(order.getUserId(), newBalance);

        String txnId = "BAL" + System.currentTimeMillis();
        log.info("[余额支付] 扣款成功, 用户 {} 剩余: {}", order.getUserId(), newBalance);
        return ChannelResult.ok(txnId);
    }
}