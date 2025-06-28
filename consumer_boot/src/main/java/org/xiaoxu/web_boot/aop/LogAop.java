package org.xiaoxu.web_boot.aop;

import cn.hutool.core.date.DateUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAop {


    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private void log(String var1, Object... var2){
        boolean enable = true;
        if(enable){
            logger.info(var1, var2);
        }
    }
//    @Pointcut("execution(* org.xiaoxu.easyexceldemo.controller.*.*(..))")
    @Pointcut("execution(* org.xiaoxu.web_boot.controller.*.*(..))")
    public void log() {

    }


    @Around("log()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
//        log("方法名称：{}", joinPoint.getSignature().getDeclaringTypeName());
//        log("方法参数：{}", joinPoint.getArgs());
        log("执行方法：{},开始时间:{}", joinPoint.getSignature().getName(), DateUtil.now());
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        log("结束方法：{}， 用时：{} 毫秒, 结束时间:{} , ", joinPoint.getSignature().getName(), endTime - startTime, DateUtil.now());
        return result;
    }

}
