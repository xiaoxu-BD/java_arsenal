package org.xiaoxu.common.exception;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.xiaoxu.common.exception.BizException;
import org.xiaoxu.common.exception.ErrorCode;
import org.xiaoxu.common.utils.Result;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常
     */
    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e) {
        log.error("业务异常: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        if (errorCode != null) {
            int code = StringUtils.isNumeric(errorCode.getCode()) ? Integer.parseInt(errorCode.getCode()) : 500;
            return Result.error(code, e.getMessage());
        }
        return Result.error(500, e.getMessage());
    }

    /**
     * 自定义消息处理异常
     */
    @ExceptionHandler(MessageProcessException.class)
    public Result<?> handleMessageProcessException(MessageProcessException e) {
        log.error("消息处理异常: {}", e.getMessage(), e);
        return Result.error(BizErrorCode.MESSAGE_PROCESS_ERROR.getCodeValue(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        FieldError fieldError = bindingResult.getFieldError();
        String message = (fieldError != null)
                ? fieldError.getField() + " " + fieldError.getDefaultMessage()
                : "参数校验失败";
        return Result.error(BizErrorCode.BAD_REQUEST.getCodeValue(), message);
    }

    /**
     * 认证失败
     */
    @ExceptionHandler(BadCredentialsException.class)
    public Result<?> handleBadCredentialsException(BadCredentialsException e) {
        log.error("认证失败: {}", e.getMessage());
        return Result.error(BizErrorCode.PASSWORD_ERROR.getCodeValue(), BizErrorCode.PASSWORD_ERROR.getMessage());
    }

    /**
     * 用户名不存在
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public Result<?> handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.error("用户不存在: {}", e.getMessage());
        return Result.error(BizErrorCode.USER_NOT_FOUND.getCodeValue(), BizErrorCode.USER_NOT_FOUND.getMessage());
    }

    /**
     * 权限不足
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限不足: {}", e.getMessage());
        return Result.error(BizErrorCode.FORBIDDEN.getCodeValue(), BizErrorCode.FORBIDDEN.getMessage());
    }

    /**
     * 认证流程内部异常
     */
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public Result<?> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException e) {
        Throwable cause = e.getCause();
        if (cause instanceof BizException biz) {
            log.warn("认证内部业务异常: {}", biz.getMessage());
            ErrorCode errorCode = biz.getErrorCode();
            int status = errorCode != null && StringUtils.isNumeric(errorCode.getCode())
                    ? Integer.parseInt(errorCode.getCode()) : 401;
            return Result.error(status, biz.getMessage());
        }
        log.error("认证内部异常", e);
        return Result.error(BizErrorCode.INTERNAL_ERROR.getCodeValue(), "登录失败，请稍后再试");
    }

    /**
     * 空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointerException(NullPointerException e) {
        log.error("系统发生空指针异常!", e);
        return Result.error(BizErrorCode.INTERNAL_ERROR.getCodeValue(), "服务器开小差了，请稍后再试");
    }

    /**
     * 资源不存在
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("请求的资源不存在: {}", e.getResourcePath());
        return Result.error(BizErrorCode.NOT_FOUND.getCodeValue(), BizErrorCode.NOT_FOUND.getMessage());
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统发生未预期的致命错误!", e);
        return Result.error(BizErrorCode.INTERNAL_ERROR.getCodeValue(), BizErrorCode.INTERNAL_ERROR.getMessage());
    }
}
