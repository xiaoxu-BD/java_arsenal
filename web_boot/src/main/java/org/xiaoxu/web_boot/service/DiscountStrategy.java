package org.xiaoxu.web_boot.service;

// 定义策略接口
public interface DiscountStrategy {
    double applyDiscount(double price);
}
