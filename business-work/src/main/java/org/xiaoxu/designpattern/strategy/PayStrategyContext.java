package org.xiaoxu.designpattern.strategy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 策略上下文 — 自动收集所有 PayStrategy 实现，按 channelCode 路由
 *
 * 原理：
 * Spring 将 List<PayStrategy> 注入所有实现类
 * @PostConstruct 时按 channelCode 建立映射
 * 调用方只需传 channelCode，无需知道具体实现
 */
@Component
@RequiredArgsConstructor
public class PayStrategyContext {

    private final List<PayStrategy> strategies;
    private final Map<String, PayStrategy> strategyMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (PayStrategy strategy : strategies) {
            strategyMap.put(strategy.channelCode(), strategy);
        }
    }

    /**
     * 按渠道编码获取策略并执行支付
     */
    public String execute(String channelCode, String orderId, String amount) {
        PayStrategy strategy = strategyMap.get(channelCode);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的支付渠道: " + channelCode);
        }
        return strategy.pay(orderId, amount);
    }
}
