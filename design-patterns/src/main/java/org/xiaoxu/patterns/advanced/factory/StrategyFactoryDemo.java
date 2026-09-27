package org.xiaoxu.patterns.advanced.factory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 策略注册表工厂演示（高级版）
 *
 * <p>运行 main 可以看到两种写法的对比：
 * switch 版每加一个渠道都要回来改核心方法；注册表版新增"秒杀五折"
 * 只需临时注册，旧代码零修改。报错信息也更友好。
 */
public class StrategyFactoryDemo {

    /** 大促当天临时加的策略：五折。不改任何旧类，报名即可参战 */
    static class SeckillStrategy implements DiscountStrategy {

        @Override
        public String code() {
            return "SECKILL";
        }

        @Override
        public BigDecimal apply(BigDecimal price) {
            return price.multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);
        }
    }

    public static void main(String[] args) {
        BigDecimal price = new BigDecimal("600");

        System.out.println("========== 1. 反面教材：switch 版 ==========");
        System.out.println("VIP 价: " + switchPrice("VIP", price));
        // 想加渠道？回来改 switch，然后全量回归

        System.out.println("\n========== 2. 注册表版 ==========");
        System.out.println("FULL_REDUCE 价: " + StrategyRegistry.require("FULL_REDUCE").apply(price));
        System.out.println("VIP         价: " + StrategyRegistry.require("VIP").apply(price));
        System.out.println("NEW_USER    价: " + StrategyRegistry.require("NEW_USER").apply(price));

        System.out.println("\n========== 3. 运行期注册新策略，旧代码零修改 ==========");
        StrategyRegistry.register(new SeckillStrategy());
        System.out.println("SECKILL     价: " + StrategyRegistry.require("SECKILL").apply(price));

        System.out.println("\n========== 4. 编码配错时的报错体验 ==========");
        try {
            StrategyRegistry.require("NOT_EXIST");
        } catch (IllegalArgumentException e) {
            System.out.println("报错清晰可排查: " + e.getMessage());
        }
    }

    /** 反面教材：每加一个渠道都要回来改这个方法，改一次测一次全量 */
    private static BigDecimal switchPrice(String code, BigDecimal price) {
        return switch (code) {
            case "FULL_REDUCE" -> price.compareTo(BigDecimal.valueOf(500)) >= 0
                    ? price.subtract(BigDecimal.valueOf(80)) : price;
            case "VIP" -> price.multiply(new BigDecimal("0.88")).setScale(2, RoundingMode.HALF_UP);
            case "NEW_USER" -> price.subtract(BigDecimal.valueOf(15)).max(new BigDecimal("0.01"));
            default -> throw new IllegalArgumentException("未知策略: " + code);
        };
    }
}
