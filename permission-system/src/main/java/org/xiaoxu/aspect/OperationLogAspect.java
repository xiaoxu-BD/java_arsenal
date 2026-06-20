package org.xiaoxu.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.xiaoxu.annotation.OperationLog;
import org.xiaoxu.pojo.SysOperationLog;
import org.xiaoxu.service.AuditLogService;

import java.lang.reflect.Method;

/**
 * 业务操作日志切面 — 拦截所有标注了 @OperationLog 的 Controller 方法
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Resource
    private AuditLogService auditLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("@annotation(org.xiaoxu.annotation.OperationLog)")
    public void pointcut() {}

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        SysOperationLog opLog = new SysOperationLog();

        // 1. 解析注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);
        opLog.setModule(annotation.module());
        opLog.setOperation(annotation.operation());
        opLog.setMethod(signature.getDeclaringTypeName() + "." + method.getName());

        // 2. 请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            opLog.setRequestUrl(request.getRequestURI());
            opLog.setRequestMethod(request.getMethod());
            opLog.setIp(getClientIp(request));
        }

        // 3. 请求参数（截断防止过长）
        try {
            Object[] args = joinPoint.getArgs();
            String params = objectMapper.writeValueAsString(args);
            opLog.setRequestParams(params.length() > 2000 ? params.substring(0, 2000) : params);
        } catch (Exception e) {
            opLog.setRequestParams("参数序列化失败");
        }

        // 4. 操作人
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            opLog.setOperator(auth.getName());
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
            opLog.setStatus(0);
            // 5. 返回结果（截断）
            try {
                String resStr = objectMapper.writeValueAsString(result);
                opLog.setResponseResult(resStr.length() > 2000 ? resStr.substring(0, 2000) : resStr);
            } catch (Exception ignored) {}
        } catch (Throwable e) {
            opLog.setStatus(1);
            opLog.setErrorMsg(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 2000)) : "未知异常");
            throw e;
        } finally {
            opLog.setCostTime(System.currentTimeMillis() - startTime);
            auditLogService.recordOperationLog(opLog);
        }

        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
