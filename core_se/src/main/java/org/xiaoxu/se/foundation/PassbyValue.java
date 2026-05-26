package org.xiaoxu.se.foundation;

import org.xiaoxu.entity.Order;
import org.xiaoxu.se.enums.OrderStatusEnum;

public class PassbyValue {
    // ========== 基本类型传递 ==========
    static void changeInt(int x) {
        x = 100;  // 修改的是副本，不影响原变量
    }

    // ========== 对象类型传递 ==========
    static void changeReference(Order order) {
        order.setStatus(OrderStatusEnum.PAID);  // 修改对象属性 → 原对象也变了
        order = new Order();                     // 重新赋值 → 不影响原引用
        order.setStatus(OrderStatusEnum.CANCELLED);
    }

    // ========== String（特殊的对象） ==========
    static void changeString(String s) {
        s = "world";  // String 不可变 + 值传递 → 原字符串不变
    }

    // ========== 数组（也是对象） ==========
    static void changeArray(int[] arr) {
        arr[0] = 999;       // 修改元素 → 原数组变了
        arr = new int[]{1}; // 重新赋值 → 不影响原引用
    }

    public static void main(String[] args) {

        // --- 基本类型 ---
        int a = 1;
        changeInt(a);
        System.out.println(a);  // 1  ← 没变

        // --- 对象类型 ---
        Order order = new Order();
        order.setStatus(OrderStatusEnum.PENDING);
        changeReference(order);
        System.out.println(order.getStatus());  // PAID ← 属性变了
        // 但 order 本身还是指向原来的对象，不是 new 出来的那个

        // --- String ---
        String s = "hello";
        changeString(s);
        System.out.println(s);  // hello ← 没变

        // --- 数组 ---
        int[] arr = {1, 2, 3};
        changeArray(arr);
        System.out.println(arr[0]);  // 999 ← 元素变了
    }

}
