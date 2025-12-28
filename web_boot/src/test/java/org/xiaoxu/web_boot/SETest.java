package org.xiaoxu.web_boot;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * @className: SETEST
 * @author: xiaoxu
 * @date: 2025/12/20 19:50
 * @Version: 1.0
 * @description:
 */
public class SETest {



    @Test
    public void testPrecision(){
        double a = 0.1;
        double  b =  0.2;
        System.out.println(a + b);
    }


    @Test
    public void testBigDecimal(){
        BigDecimal a = BigDecimal.valueOf(0.1);
        BigDecimal b = BigDecimal.valueOf(0.2);
        System.out.println(a.add(b));
    }
    @Test
    public  void testBigDecimal2(){
        BigDecimal a = new BigDecimal("0.1");
        BigDecimal b = new BigDecimal("0.2");
        //not use equals change compareTo
        int res = a.compareTo(b);
        if (res == 0) {
            System.out.println("相等");
        } else if (res > 0) {
            System.out.println("a大于b");
        }else {
            System.out.println("a小于b");
        }
    }

}
