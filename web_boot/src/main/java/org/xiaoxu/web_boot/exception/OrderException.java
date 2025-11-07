package org.xiaoxu.web_boot.exception;

import org.xiaoxu.web_boot.enums.ErrorCode;

/**
 * @className: OrderException
 * @author: xiaoxu
 * @date: 2025/11/3 10:52
 * @Version: 1.0
 * @description:
 */
public class OrderException extends BizException{
    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }
}
