package org.xiaoxu.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.assertj.core.condition.Join;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @className: OpLogAspect
 * @author: xiaoxu
 * @date: 2025/11/19 15:48
 * @Version: 1.0
 * @description:
 */
@Aspect
@Component
public class OpLogAspect {

   private static final Logger LOGGER =  LoggerFactory.getLogger(OpLogAspect.class);




   @Around("@annotation(org.xiaoxu.aop.OpLog)")
    public Object logs(ProceedingJoinPoint joinPoint){

       Long time = System.currentTimeMillis();
       Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

       OpLog annotation = method.getAnnotation(OpLog.class);

       Object result = null ;

       try {
           result = joinPoint.proceed();
       } catch (Throwable throwable) {
           throwable.printStackTrace();
       }

       time = System.currentTimeMillis() - time;

       LOGGER.info("[日志记录]方法名称:{},场景 {}, 耗时{}ms",method.getName(),annotation.scene(),time);

       return result;
   }
}
