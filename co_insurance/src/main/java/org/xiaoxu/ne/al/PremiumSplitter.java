package org.xiaoxu.ne.al;

import org.xiaoxu.ne.entity.CoParty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class PremiumSplitter {

    public static List<BigDecimal> split(BigDecimal total, List<CoParty> parties) {

        List<BigDecimal> result = new ArrayList<>();
        BigDecimal assigned = BigDecimal.ZERO;

        for (int i = 0; i < parties.size() - 1; i++) {
            BigDecimal share = total
                    .multiply(parties.get(i).getShareRatio())
                    .setScale(2, RoundingMode.FLOOR);

            result.add(share);
            assigned = assigned.add(share);
        }

        BigDecimal last = total.subtract(assigned);
        result.add(last);

        BigDecimal sum = result.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(total) != 0) {
            throw new RuntimeException("精度错误");
        }

        return result;
    }
}