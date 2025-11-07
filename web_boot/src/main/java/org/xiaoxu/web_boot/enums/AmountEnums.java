package org.xiaoxu.web_boot.enums;

public enum AmountEnums implements ErrorCode{
    AMOUNT_NOT_ENOUGH("10001", "余额不足"),
    ;

    private String code;

    private String message;

    AmountEnums(String code, String message) {
        this.code = code;
        this.message = message;
    }
    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getCode() {
        return  code;
    }
}
