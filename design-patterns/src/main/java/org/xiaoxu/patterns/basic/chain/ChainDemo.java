package org.xiaoxu.patterns.basic.chain;

/**
 * 责任链模式演示（初级版）
 *
 * <p>课堂演示点：申请人只把单子递给"组长"这一个入口，
 * 1 天组长直接批、3 天传到经理、5 天传到总监、10 天谁也批不了。
 *
 * <p>日常同款：Servlet Filter、Netty Pipeline、网关过滤器。
 */
public class ChainDemo {

    public static void main(String[] args) {
        // 组链：组长 -> 经理 -> 总监
        Approver chain = new GroupLeader();
        chain.setNext(new Manager())
             .setNext(new Director());

        System.out.println("========== 请假 1 天 ==========");
        chain.handle(new LeaveRequest("张三", 1));

        System.out.println("\n========== 请假 3 天 ==========");
        chain.handle(new LeaveRequest("李四", 3));

        System.out.println("\n========== 请假 5 天 ==========");
        chain.handle(new LeaveRequest("王五", 5));

        System.out.println("\n========== 请假 10 天 ==========");
        chain.handle(new LeaveRequest("赵六", 10));
    }
}
