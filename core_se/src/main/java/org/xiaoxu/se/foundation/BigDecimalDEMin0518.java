package org.xiaoxu.se.foundation;

import java.math.BigDecimal;

public class BigDecimalDEMin0518 {

    public static void main(String[] args) {

        BigDecimal bigDecimal01 = BigDecimal.valueOf(0.1);
        BigDecimal bigDecimal02 = new BigDecimal("0.10");
        System.out.println(bigDecimal01);
        System.out.println(bigDecimal02);

        if (bigDecimal02.compareTo(bigDecimal01) == 0) {
            System.out.println("compareTo 是忽略的精度");
        }
        if (!bigDecimal02.equals(bigDecimal01)){
            System.out.println("equals 比较值 和 比较精度");
        }
    }
}
