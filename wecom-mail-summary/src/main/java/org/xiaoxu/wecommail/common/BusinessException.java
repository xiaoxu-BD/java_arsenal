package org.xiaoxu.wecommail.common;

import lombok.Getter;

/**
 * 业务异常,由全局异常处理器转为统一响应
 */
@Getter
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
