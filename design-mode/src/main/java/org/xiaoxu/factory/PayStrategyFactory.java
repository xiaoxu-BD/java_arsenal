package org.xiaoxu.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xiaoxu.strategy.PayStrategy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @className: PayStrategyFactory
 * @author: xiaoxu
 * @date: 2025/8/7 7:56
 * @Version: 1.0
 * @description:
 */
@Component
public class PayStrategyFactory {

    private final Map<String, PayStrategy> payFactory = new ConcurrentHashMap<>();

    @Autowired
    public PayStrategyFactory(List<PayStrategy> payStrategies) {
        // 这里 Spring 会注入所有 PayStrategy 的实现类 Bean
        // 并自动调用它们的无参构造函数，将它们实例化
        // 并将它们的 Bean 名称作为 Map 的 key，将它们的实例作为 Map 的 value
        for (PayStrategy payStrategy : payStrategies) {
            payFactory.put(payStrategy.getType(),payStrategy);
        }
    }

    public PayStrategy getPayStrategy(String type){
        PayStrategy payStrategy = payFactory.get(type);
        if (payStrategy == null){
            throw new IllegalArgumentException("不支持该支付方式");
        }
        return payStrategy;
    }

}
