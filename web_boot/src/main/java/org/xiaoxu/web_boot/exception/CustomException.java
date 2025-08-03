package org.xiaoxu.web_boot.exception;

/**
 * @className: CustomException
 * @author: xiaoxu
 * @date: 2025/7/31 7:19
 * @Version: 1.0
 * @description:
 */
public class CustomException extends RuntimeException{
    private int code;

    public CustomException(int code, String message) {
        super(message);
        this.code = code;
    }
    public int getCode() {
        return code;
    }
}
