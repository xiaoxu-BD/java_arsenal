package org.xiaoxu.patterns.basic.chain;

/**
 * 审批人抽象类（Handler）
 *
 * <p>责任链模式的两个要素：
 * <ol>
 *   <li>{@code next} 指向下一个处理者，串成一条链；</li>
 *   <li>{@code handle} 里"能批就批，批不了传给下家"。</li>
 * </ol>
 * 发起人不关心最终是谁批的，甚至不知道链上有几个人。
 */
public abstract class Approver {

    /** 下一个处理者：由 setNext 串起来 */
    protected Approver next;

    /** 串链语法糖：a.setNext(b) 返回 b，可以一路 . 下去 */
    public Approver setNext(Approver next) {
        this.next = next;
        return next;
    }

    /** 处理请求：能处理就处理，否则转给下家 */
    public abstract void handle(LeaveRequest request);
}
