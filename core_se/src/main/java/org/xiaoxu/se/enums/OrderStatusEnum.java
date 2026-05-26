package org.xiaoxu.se.enums;

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