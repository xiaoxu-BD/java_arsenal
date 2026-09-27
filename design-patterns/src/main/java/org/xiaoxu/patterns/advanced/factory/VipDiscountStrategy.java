package org.xiaoxu.patterns.advanced.factory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 具体策略：会员 88 折 */
public class VipDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal RATE = new BigDecimal("0.88");

    @Override
    public String code() {
        return "VIP";
    }

    @Override
    public BigDecimal apply(BigDecimal price) {
        // 金额：保留 2 位小数、四舍五入，规则全项目统一
        return price.multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
