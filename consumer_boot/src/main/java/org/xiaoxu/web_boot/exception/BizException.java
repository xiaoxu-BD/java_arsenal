package org.xiaoxu.web_boot.exception;

/**
 * @className: BizException
 * @author: xiaoxu
 * @date: 2025/9/2 9:57
 * @Version: 1.0
 * @description:
 */
public class BizException extends RuntimeException {

    private int code;

    public BizException(String message) {
        super(message);
    }

    public BizException(int code,String message) {
        super(message);
        this.code = code;
    }


    public int getCode() {
        return code;
    }
}
