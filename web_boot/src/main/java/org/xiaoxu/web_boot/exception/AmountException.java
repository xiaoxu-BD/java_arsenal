package org.xiaoxu.web_boot.exception;

import org.xiaoxu.web_boot.enums.ErrorCode;

/**
 * @className: AmountException
 * @author: xiaoxu
 * @date: 2025/11/3 10:45
 * @Version: 1.0
 * @description:
 */
public class AmountException extends BizException{
    public AmountException(ErrorCode errorCode) {
        super(errorCode);
    }
}
