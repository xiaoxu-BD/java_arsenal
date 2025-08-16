package org.xiaoxu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xiaoxu.factory.PayStrategyFactory;
import org.xiaoxu.strategy.PayStrategy;
@Component
public class PayService {
    private final PayStrategyFactory strategyFactory;
    @Autowired
    public PayService(PayStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

   public void pay(String type,double amount){
       PayStrategy payStrategy = strategyFactory.getPayStrategy(type);
       if (payStrategy == null){
           throw new IllegalArgumentException("不支持该支付类型");
       }
       payStrategy.pay(amount);
   }
}
