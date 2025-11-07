package org.xiaoxu.web_boot;

import com.google.common.collect.Lists;
import org.xiaoxu.web_boot.entity.practice.Item;
import org.xiaoxu.web_boot.entity.practice.Order;
import org.xiaoxu.web_boot.entity.practice.OrderSummary;
import org.xiaoxu.web_boot.entity.practice.User;
import org.xiaoxu.web_boot.service.impl.DoubleElevenDiscountStrategy;
import org.xiaoxu.web_boot.service.impl.NewUserDiscountStrategy;
import org.xiaoxu.web_boot.service.impl.NoDiscountStrategy;
import org.xiaoxu.web_boot.service.practice.OrderService;
import org.xiaoxu.web_boot.service.practice.OrderServiceImpl;

import java.util.List;

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
//        // 场景1：一个普通用户下单，无折扣
//        Order order1 = new Order(100.0, new NoDiscountStrategy());
//        System.out.println("普通用户最终价格: " + order1.getFinalPrice()); // 输出: 100.0
//
//        // 场景2：一个新用户下单，享受9折
//        Order order2 = new Order(100.0, new NewUserDiscountStrategy());
//        System.out.println("新用户最终价格: " + order2.getFinalPrice()); // 输出: 90.0
//
//        // 场景3：双十一活动，同一个订单，临时改变策略
//        Order order3 = new Order(100.0, new NewUserDiscountStrategy()); // 开始时是新用户
//        System.out.println("双十一前价格: " + order3.getFinalPrice()); // 输出: 90.0
//
//        // 双十一开始了！动态切换为双十一策略
//        order3.setStrategy(new DoubleElevenDiscountStrategy());
//        System.out.println("双十一当天价格: " + order3.getFinalPrice()); // 输出: 50.0




                OrderService orderService = new OrderServiceImpl();

                // 构造用户
                User u1 = new User();
                u1.setId(1L);
                u1.setName("小明");
                u1.setLevel("vip");

                User u2 = new User();
                u2.setId(2L);
                u2.setName("小红");
                u2.setLevel("svip");

                // 构造商品
                Item i1 = new Item();
                i1.setName("苹果");
                i1.setPrice(3.5);
                i1.setQuantity(10);

                Item i2 = new Item();
                i2.setName("香蕉");
                i2.setPrice(2.0);
                i2.setQuantity(5);

                Item i3 = new Item();
                i3.setName("电脑");
                i3.setPrice(5999);
                i3.setQuantity(1);

                // 构造订单
                Order o1 = new Order();
                o1.setOrderId("O1001");
                o1.setUser(u1);
                o1.setItems(Lists.newArrayList(i1, i2));

                Order o2 = new Order();
                o2.setOrderId("O1002");
                o2.setUser(u2);
                o2.setItems(Lists.newArrayList(i3));

                List<Order> orders = Lists.newArrayList(o1, o2);

                // 执行订单处理
                List<OrderSummary> result = orderService.processOrders(orders);

                System.out.println("\n===== 最终结果输出 =====");
                result.forEach(System.out::println);
            }
        }





