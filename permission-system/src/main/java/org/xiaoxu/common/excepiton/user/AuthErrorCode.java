package org.xiaoxu.common.excepiton.user;

import lombok.Getter;
import org.xiaoxu.common.excepiton.ErrorCode;
@Getter
public enum AuthErrorCode implements ErrorCode {


    USER_NAME_NOT_EXIST("1404","用户名不存在"),
    EMAIL_AUTH_CODE_ERROR("1452","邮箱验证码错误")
    ;






    private String code;

    private String message;

    AuthErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
