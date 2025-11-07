package org.xiaoxu.web_boot.exception;

import org.xiaoxu.web_boot.enums.ErrorCode;

/**
 * @className: BizException
 * @author: xiaoxu
 * @date: 2025/11/3 10:41
 * @Version: 1.0
 * @description:
 */
public class BizException extends RuntimeException {
    private ErrorCode errorCode;


    public BizException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }


}
