package org.xiaoxu.web_boot;

import org.xiaoxu.web_boot.service.impl.DoubleElevenDiscountStrategy;
import org.xiaoxu.web_boot.service.impl.NewUserDiscountStrategy;
import org.xiaoxu.web_boot.service.impl.NoDiscountStrategy;
import org.xiaoxu.web_boot.strategy.Order;

/**
 * @className: Main
 * @author: xiaoxu
 * @date: 2025/6/29 9:34
 * @Version: 1.0
 * @description:
 */
public class Main {
    public static void main(String[] args) {
//        Payment alipay = PaymentFactory.createPayment("aliPay");
//            double res = alipay.calculatePayment(100.0);
//            System.out.println(res);
//        // 场景2：用户选择微信
//        Payment wechatPay = PaymentFactory.createPayment("weChatPay");
//
//            double res2 = wechatPay.calculatePayment(200.0);
//            System.out.println(res2);
        // 场景1：一个普通用户下单，无折扣
        Order order1 = new Order(100.0, new NoDiscountStrategy());
        System.out.println("普通用户最终价格: " + order1.getFinalPrice()); // 输出: 100.0

        // 场景2：一个新用户下单，享受9折
        Order order2 = new Order(100.0, new NewUserDiscountStrategy());
        System.out.println("新用户最终价格: " + order2.getFinalPrice()); // 输出: 90.0

        // 场景3：双十一活动，同一个订单，临时改变策略
        Order order3 = new Order(100.0, new NewUserDiscountStrategy()); // 开始时是新用户
        System.out.println("双十一前价格: " + order3.getFinalPrice()); // 输出: 90.0

        // 双十一开始了！动态切换为双十一策略
        order3.setStrategy(new DoubleElevenDiscountStrategy());
        System.out.println("双十一当天价格: " + order3.getFinalPrice()); // 输出: 50.0
    }




    }

