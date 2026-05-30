package org.xiaoxu.se.enums;

/**
 * 枚举 + 抽象方法：每个实例各自实现
 * 本质是匿名内部类
 */
public enum Payment {

    PAYPAL {
        @Override
        double getFee(double amount) { return amount * 0.03; }
    },
    WECHAT {
        @Override
        double getFee(double amount) { return 0; }
    },
    BANK {
        @Override
        double getFee(double amount) { return amount * 0.025; }
    };

    abstract double getFee(double amount);
}
