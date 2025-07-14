package org.xiaoxu.web_boot.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.xiaoxu.web_boot.service.DiscountStrategy;

/**
 * @className: DoubleElevenDiscountStrategy
 * @author: xiaoxu
 * @date: 2025/6/29 9:51
 * @Version: 1.0
 * @description:
 */
@Slf4j
public class DoubleElevenDiscountStrategy implements DiscountStrategy {
    @Override
    public double applyDiscount(double price) {
        log.info("双11 5折:{}",price);
        if (price > 0) {
            return price * 0.5;
        }
        return 0;
    }
}
