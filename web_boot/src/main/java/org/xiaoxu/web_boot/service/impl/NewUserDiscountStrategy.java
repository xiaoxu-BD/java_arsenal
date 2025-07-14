package org.xiaoxu.web_boot.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.xiaoxu.web_boot.service.DiscountStrategy;

/**
 * @className: NewUserDiscountStrategy
 * @author: xiaoxu
 * @date: 2025/6/29 9:50
 * @Version: 1.0
 * @description: 新人九折
 */
@Slf4j
public class NewUserDiscountStrategy implements DiscountStrategy {
    @Override
    public double applyDiscount(double price) {
        log.info("新人九折:{}",price);
        if (price > 0) {
            return price * 0.9;
        }
        return 0;
    }
}
