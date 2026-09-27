package org.xiaoxu.patterns.advanced.state;

/**
 * 订单状态枚举：状态机里的"状态"本身
 *
 * <p>业务流转：待支付 -> 已支付 -> 已发货 -> 已完成；未支付/已支付均可取消。
 */
public enum OrderStatus {

    CREATED("待支付"),
    PAID("已支付"),
    SHIPPED("已发货"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String desc;

    OrderStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
