package org.xiaoxu.web_boot.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.xiaoxu.web_boot.common.Result;

/**
 * @className: GlobalException
 * @author: xiaoxu
 * @date: 2025/9/2 9:58
 * @Version: 1.0
 * @description:
 */
@RestControllerAdvice

public class GlobalException {



    @ExceptionHandler(BizException.class)
    public Result<?> exceptionHandler(Exception e) {

        return Result.error(e.getMessage());
    }

    // 捕获所有未知异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(500, "系统内部错误，请联系管理员");
    }
}
