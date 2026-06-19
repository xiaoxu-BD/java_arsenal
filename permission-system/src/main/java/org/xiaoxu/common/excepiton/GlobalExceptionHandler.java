package org.xiaoxu.common.excepiton;

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
import org.xiaoxu.common.utils.Result;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);



    @ExceptionHandler(BizException.class)
    public Result<?> exceptionThrow (BizException bizException){
        log.error("业务异常{}",bizException.getMessage());
        if (!StringUtils.isNumeric(bizException.getErrorCode().getCode())){
            return Result.error(500,bizException.getMessage());
        }
      return   Result.error(Integer.parseInt(bizException.getErrorCode().getCode()),bizException.getMessage());
    }


    // 2. 捕获空指针异常
    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointerException(NullPointerException e) {
        log.error("系统发生空指针异常!", e);
        return Result.error(500, "服务器开小差了，请稍后再试");
    }

    // 3. 捕获所有兜底的未知异常（防止向前端抛出 Tomcat 堆栈信息）
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统发生未预期的致命错误!", e);
        return Result.error(500, "服务器内部错误，请联系管理员");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Result<?> handleCredentialException(BadCredentialsException exception){

        log.error("未经认证的异常：{}", exception.getMessage());
        return Result.error(401,"用户名或者密码错误~");
    }


    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handlerDeniedException(AccessDeniedException exception){

        log.error("权限不足：{}", exception.getMessage());
        return Result.error(403,exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        // 1. 获取 BindingResult，里面包含了校验失败的所有细节
        BindingResult bindingResult = e.getBindingResult();

        // 2. 获取第一个错误信息（或者你也可以通过 stream 拼接所有错误）
        FieldError fieldError = bindingResult.getFieldError();
        String message = (fieldError != null)
                ? fieldError.getField() + " " + fieldError.getDefaultMessage()
                : "参数校验失败";

        // 3. 返回 400 Bad Request 状态码
        return Result.error(400, message);
    }


    @ExceptionHandler(UsernameNotFoundException.class)
    public Result<?> handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.error("用户名不存在 : {} ",e.getMessage());
        return Result.error(401, "用户不存在");
    }

    /**
     * 认证流程内部异常兜底。
     * <p>
     * Spring Security 的 {@code DaoAuthenticationProvider} 会把 {@link UserDetailsService}
     * 内部抛出的任何非 {@code AuthenticationException} 异常包装成 {@code InternalAuthenticationServiceException}，
     * 这会绕过我们对 {@link BizException} 的处理。这里把 cause 链拆开：
     * <ul>
     *   <li>cause 是 {@link BizException} → 用业务错误码 + 业务消息返回（与正常流程一致）</li>
     *   <li>否则 → 返回 500 通用错误，避免泄漏堆栈</li>
     * </ul>
     */
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public Result<?> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException e) {
        Throwable cause = e.getCause();
        if (cause instanceof BizException biz) {
            log.warn("认证内部业务异常: {}", biz.getMessage());
            String code = biz.getErrorCode() == null ? null : biz.getErrorCode().getCode();
            int status = StringUtils.isNumeric(code) ? Integer.parseInt(code) : 401;
            return Result.error(status, biz.getMessage());
        }
        log.error("认证内部异常", e);
        return Result.error(500, "登录失败，请稍后再试");
    }

    // 捕获 Spring 6 静态资源/接口找不到异常，否则会被下方 Exception.class 兜底误判为 500
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("请求的资源不存在 : {} ", e.getResourcePath());
        return Result.error(404, "请求的资源不存在");
    }
//InternalAuthenticationServiceException




}
