package org.xiaoxu.web_boot.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @className: ParamCheckAspect
 * @author: xiaoxu
 * @date: 2025/9/2 10:46
 * @Version: 1.0
 * @description:
 */
@Aspect
@Component
public class ParamCheckAspect {


    private static final Logger log = LoggerFactory.getLogger(ParamCheckAspect.class);

    /**
     * 定义切点
     */
    @Pointcut("execution(* org.xiaoxu.web_boot.controller.*.*(..))")
    public void checkParamsIsLegal(){
    }


    /**
     * 前面逻辑
     */

    @Around("@annotation(org.xiaoxu.web_boot.aop.ParamCheck)")
    public Object checkParams(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();

        ParamCheck paramCheck = signature.getMethod().getAnnotation(ParamCheck.class);
        if (paramCheck != null && paramCheck.ignore()){
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg == null) {
                    log.error("params are not allowed");
                    throw new IllegalArgumentException("参数不能为空");
                }
            }
        }
        //校验参数
        return joinPoint.proceed();
    }
}
