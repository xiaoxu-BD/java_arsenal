package org.xiaoxu.web_boot.enums;

public enum OrderEnums implements ErrorCode{
    ORDER_PARAM_ERROR("订单提交参数错误", "10001"),
    ORDER_NOT_EXIST("订单不存在", "10002"),
    ORDER_NOT_PAID("订单未支付", "10003"),
    ITEM_NAME_ERROR("商品名称错误","1004")

    ;

    private String message;
    private String code;
    private OrderEnums(String message, String code) {
        this.message = message;
        this.code = code;
    }
    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getCode() {
        return code;
    }
}
