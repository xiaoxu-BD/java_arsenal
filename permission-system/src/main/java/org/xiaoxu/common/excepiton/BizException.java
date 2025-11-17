package org.xiaoxu.common.excepiton;

/**
 * @className: BizException
 * @author: xiaoxu
 * @date: 2025/11/17 15:28
 * @Version: 1.0
 * @description:
 */
public class BizException extends RuntimeException{

    private ErrorCode errorCode;



    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }



    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BizException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
