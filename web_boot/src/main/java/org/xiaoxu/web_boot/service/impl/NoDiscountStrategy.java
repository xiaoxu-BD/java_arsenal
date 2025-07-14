package org.xiaoxu.web_boot.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.xiaoxu.web_boot.service.DiscountStrategy;

/**
 * @className: NoDiscountStrategy
 * @author: xiaoxu
 * @date: 2025/6/29 9:48
 * @Version: 1.0
 * @description: 无折扣的策略
 */
@Slf4j
public class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public double applyDiscount(double price) {
        log.info("无折扣:{}",price);
        if (price > 0) {
            return price;
        }
        return 0;
    }
}
