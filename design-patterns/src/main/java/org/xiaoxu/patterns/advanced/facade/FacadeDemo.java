package org.xiaoxu.patterns.advanced.facade;

import java.math.BigDecimal;

/**
 * 业务门面演示（高级版）
 *
 * <p>调用方只有一行；对比"自己编排三个子系统"的写法，
 * 顺序、异常、单号生成全部被门面收编。
 */
public class FacadeDemo {

    public static void main(String[] args) {
        OrderFacade facade = new OrderFacade();

        String orderNo = facade.placeOrder("SKU-1001", 1, new BigDecimal("499.00"), "上海市浦东新区");
        System.out.println("下单成功: " + orderNo);

        System.out.println("\n========== 参数非法直接拦截 ==========");
        try {
            facade.placeOrder("SKU-1001", 0, BigDecimal.ZERO, "北京市");
        } catch (IllegalArgumentException e) {
            System.out.println("下单失败: " + e.getMessage());
        }
    }
}
