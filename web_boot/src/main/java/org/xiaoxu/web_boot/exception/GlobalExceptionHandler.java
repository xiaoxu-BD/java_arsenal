package org.xiaoxu.web_boot.exception;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.xiaoxu.web_boot.common.Result;
import org.xiaoxu.web_boot.utils.EmailUtil;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * @className: GlobalExceptionHandler
 * @author: xiaoxu
 * @date: 2025/8/10 9:50
 * @Version: 1.0
 * @description:
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final EmailUtil emailUtil;

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<?> handleException(Exception e) {
        log.error("系统异常：", e);
        // 发送邮件
        String subject = "【系统异常报警】" + e.getClass().getSimpleName();
        String content = "当前时间: " + LocalDateTime.now() + "\n" + "系统异常信息: " + e.getMessage() + "\n\n详细堆栈:\n" + getStackTrace(e);
        emailUtil.sendErrorMail("tingqq7@gmail.com",subject,content);

        return Result.success("系统异常，请稍后再试");
    }

    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public Result<?> handleRuntimeException(CustomException e) {
        return Result.error(e.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        HashMap<String, String> errorMap = Maps.newHashMapWithExpectedSize(16);
        ex.getBindingResult().getAllErrors().forEach(error->{

            FieldError fieldError = (FieldError) error;
            String field = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            errorMap.put(field,message);
        });
        return Result.error(400, "参数错误", errorMap);
    }

    private String getStackTrace(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

}
