package org.xiaoxu.web_boot.strategy;

import org.xiaoxu.web_boot.service.DiscountStrategy;

/**
 * @className: Order
 * @author: xiaoxu
 * @date: 2025/6/29 9:52
 * @Version: 1.0
 * @description:
 */
public class Order {
    private final double originPrice;
    private DiscountStrategy discountStrategy;

    public Order(double originPrice, DiscountStrategy discountStrategy) {
        this.originPrice = originPrice;
        this.discountStrategy = discountStrategy;
    }

    //允许在运行是动态改变策略
    public void setStrategy(DiscountStrategy discountStrategy){
        this.discountStrategy = discountStrategy;
    }

    //计算最终价格 委托到具体的策略对象
    public double getFinalPrice() {
        return discountStrategy.applyDiscount(originPrice);
    }
}
