package org.xiaoxu.wecommail.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException e) {
        log.warn("业务校验不通过: {}", e.getMessage());
        return ApiResponse.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnexpected(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.error("系统异常: " + e.getMessage());
    }
}
