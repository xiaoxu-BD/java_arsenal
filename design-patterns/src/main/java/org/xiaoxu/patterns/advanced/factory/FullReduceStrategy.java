package org.xiaoxu.patterns.advanced.factory;

import java.math.BigDecimal;

/** 具体策略：满 500 减 80 */
public class FullReduceStrategy implements DiscountStrategy {

    private static final BigDecimal THRESHOLD = BigDecimal.valueOf(500);
    private static final BigDecimal REDUCTION = BigDecimal.valueOf(80);

    @Override
    public String code() {
        return "FULL_REDUCE";
    }

    @Override
    public BigDecimal apply(BigDecimal price) {
        return price.compareTo(THRESHOLD) >= 0
                ? price.subtract(REDUCTION)
                : price;
    }
}
