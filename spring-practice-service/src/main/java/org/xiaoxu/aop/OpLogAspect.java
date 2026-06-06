package org.xiaoxu.aop;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xiaoxu.mapper.OperationLogMapper;
import org.xiaoxu.pojo.OperationLog;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;

/**
 * @className: OpLogAspect
 * @author: xiaoxu
 * @date: 2025/11/19 15:48
 * @Version: 1.0
 * @description: 操作日志切面，记录方法调用信息并入库
 */
@Aspect
@Component
public class OpLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpLogAspect.class);

    @Autowired
    private OperationLogMapper operationLogMapper;


    @Around("@annotation(org.xiaoxu.aop.OpLog)")
    public Object logs(ProceedingJoinPoint joinPoint) {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        OpLog annotation = signature.getMethod().getAnnotation(OpLog.class);
        String sceneDesc = annotation.scene();


        OperationLog log = new OperationLog();
        log.setClassName(className);
        log.setMethodName(methodName);
        log.setCreateTime(LocalDateTime.now());
        log.setSceneDesc(sceneDesc);

        // 记录请求参数
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                log.setParams(JSON.toJSONString(args));
            }
        } catch (Exception e) {
            log.setParams("参数序列化失败");
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
            // 记录返回结果
            try {
                log.setResult(JSON.toJSONString(result));
            } catch (Exception e) {
                log.setResult("结果序列化失败");
            }
        } catch (Throwable throwable) {
            log.setExceptionMsg(throwable.getMessage());
            LOGGER.error("[操作日志]方法执行异常: {}.{}", className, methodName, throwable);
        }

        log.setCostTimeMs(System.currentTimeMillis() - start);

        // 异步入库，避免影响主业务性能；简单场景直接同步插入
        try {
            operationLogMapper.insert(log);
        } catch (Exception e) {
            LOGGER.error("[操作日志]日志入库失败: {}.{}", className, methodName, e);
        }

        LOGGER.info("[操作日志]方法:{}.{}, 场景:{}, 耗时:{}ms",
                className, methodName,
                signature.getMethod().getAnnotation(OpLog.class).scene(),
                log.getCostTimeMs());

        return result;
    }
}
