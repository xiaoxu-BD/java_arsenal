package org.xiaoxu.patterns.advanced.factory;

import java.math.BigDecimal;

/** 具体策略：新用户立减 15（最低 1 分钱，不出现 0 元单） */
public class NewUserStrategy implements DiscountStrategy {

    private static final BigDecimal REDUCTION = BigDecimal.valueOf(15);
    private static final BigDecimal MIN_PAY = new BigDecimal("0.01");

    @Override
    public String code() {
        return "NEW_USER";
    }

    @Override
    public BigDecimal apply(BigDecimal price) {
        BigDecimal result = price.subtract(REDUCTION);
        return result.compareTo(MIN_PAY) < 0 ? MIN_PAY : result;
    }
}
