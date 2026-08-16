package org.xiaoxu.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// exception/GlobalExceptionHandler.java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Map<String, String>> handleBiz(BizException e) {
        return ResponseEntity.badRequest().body(
            Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAll(Exception e) {
        log.error("未知异常", e);
        return ResponseEntity.status(500).body(
            Map.of("error", "系统繁忙，请稍后重试"));
    }
}