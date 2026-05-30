package org.xiaoxu.enums;

/**
 * 订单状态枚举
 *
 * TypeHandler 演示用：
 * - 数据库存储 code（Integer 类型）
 * - Java 侧通过 TypeHandler 自动转换为枚举对象，再通过 getDesc() 获取中文描述
 */
public enum OrderStatusEnum {

    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    SHIPPED(2, "已发货"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final Integer code;
    private final String desc;

    OrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据 code 反查枚举（TypeHandler 查询时调用）
     *
     * @param code 数据库存储的整数值
     * @return 对应的枚举，找不到时返回 null
     */
    public static OrderStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
