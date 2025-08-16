package org.xiaoxu.web_boot.aop.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.xiaoxu.web_boot.entity.OperationLog;
import org.xiaoxu.web_boot.mapper.OperationLogMapper;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class ControllerLogAspect {

    @Resource
    private OperationLogMapper logMapper;

    @Pointcut("execution(* org.xiaoxu.web_boot.controller..*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        OperationLog logEntry = new OperationLog();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Object[] args = pjp.getArgs();

        logEntry.setClassName(pjp.getTarget().getClass().getName());
        logEntry.setMethodName(method.getName());
        logEntry.setParams(toJsonSafe(args));
        logEntry.setCreateTime(LocalDateTime.now());

        try {
            Object result = pjp.proceed();  // 执行原始方法
            long cost = System.currentTimeMillis() - start;
            logEntry.setCostTimeMs(cost);
            logEntry.setResult(toJsonSafe(result));
            logMapper.insert(logEntry);
            return result;
        } catch (Throwable ex) {
            long cost = System.currentTimeMillis() - start;
            logEntry.setCostTimeMs(cost);
            logEntry.setExceptionMsg(ex.getMessage());
            logMapper.insert(logEntry);
            throw ex;
        }
    }
    private String toJsonSafe(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "序列化失败: " + e.getMessage();
        }
    }
}
