package org.xiaoxu.patterns.advanced.prototype;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 深拷贝工具 + 防御性拷贝演示（高级版）
 *
 * <p>业务背景：退款单要基于原订单生成一份"快照"，之后原订单怎么改都不能影响快照。
 * 运行 main 可以看到：浅拷贝的快照跟着原单一起变（事故现场），
 * 深拷贝快照 + 防御性 getter 才算真正隔离。
 *
 * <p>注：Order 是本类的嵌套类，demo 方法可以直接访问它的私有字段，
 * 这样"浅拷贝共享引用"这一行为才能被直观观察到。
 */
public class DeepCopyDemo {

    /** 订单：内含可变集合和嵌套对象，典型的"对象图" */
    static class Order implements Serializable {
        private String orderNo;
        private List<String> items;      // 故意不做防御性拷贝，用于演示浅拷贝的坑
        private Address address;

        Order(String orderNo, List<String> items, Address address) {
            this.orderNo = orderNo;
            this.items = items;
            this.address = address;
        }

        /** 防御性 getter：对外只给只读副本，外部改不到内部 list */
        List<String> viewItems() {
            return List.copyOf(items);
        }

        Address getAddress() { return address; }
        String getOrderNo() { return orderNo; }
        void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    }

    /** 嵌套对象：深拷贝的关键考察点 */
    static class Address implements Serializable {
        private final String city;

        Address(String city) { this.city = city; }

        String getCity() { return city; }
    }

    public static void main(String[] args) {
        Order original = new Order("SO-001",
                new ArrayList<>(List.of("键盘", "显示器")), new Address("上海"));

        System.out.println("========== 1. 浅拷贝：事故现场 ==========");
        // 浅拷贝：三个字段全是"复制地址"，两个 Order 共享同一个 List、同一个 Address
        Order shallow = new Order(original.getOrderNo(), original.items, original.address);
        shallow.items.add("误加的商品");
        System.out.println("往浅拷贝里加了 1 件商品后，原单商品数 = "
                + original.items.size() + "（应为 2，被污染）");

        System.out.println("\n========== 2. 深拷贝：真正隔离 ==========");
        Order snapshot = DeepCopyUtil.deepCopy(original);
        snapshot.items.add("退款备注项");
        snapshot.setOrderNo("RF-001");
        System.out.println("快照: 单号=" + snapshot.getOrderNo()
                + ", 商品数=" + snapshot.items.size());
        System.out.println("原单: 单号=" + original.getOrderNo()
                + ", 商品数=" + original.items.size() + "（未受影响）");
        System.out.println("两者的 Address 是同一个对象? "
                + (snapshot.getAddress() == original.getAddress()));

        System.out.println("\n========== 3. 防御性 getter ==========");
        try {
            original.viewItems().add("外部注入");
        } catch (UnsupportedOperationException e) {
            System.out.println("外部想通过 getter 改内部 list 被拒绝");
        }
    }
}
