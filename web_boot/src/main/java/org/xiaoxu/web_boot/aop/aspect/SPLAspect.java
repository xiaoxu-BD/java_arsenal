package org.xiaoxu.web_boot.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.xiaoxu.web_boot.aop.SPLParams;

import java.lang.reflect.Method;

/**
 * @className: SPLAspect
 * @author: xiaoxu
 * @date: 2025/9/6 12:13
 * @Version: 1.0
 * @description:
 */
@Component
@Aspect
public class SPLAspect {

    private final ExpressionParser parser = new SpelExpressionParser();



    @Pointcut("execution(* org.xiaoxu.web_boot.controller.HelloController.*(..))")
    public void pointCut() {
    }


    @Around("@annotation(splParams)")
    public Object splRecord(ProceedingJoinPoint jointPoint, SPLParams splParams) throws Throwable {
        MethodSignature signature = (MethodSignature)jointPoint.getSignature();
        Method method = signature.getMethod();

        String[] parameterNames = signature.getParameterNames();
        Object[] args = jointPoint.getArgs();

        //构建SPL上下文:
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }


        // 也可以加入一些固定变量，比如方法名
        context.setVariable("methodName", method.getName());

        // 3. 解析 SpEL 表达式
        String spelExpression = splParams.value();
        Expression expression = parser.parseExpression(spelExpression);
        // 4. 执行 SpEL 表达式，得到结果（String）
        String result = expression.getValue(context, String.class);

        // 5. 打印日志（或做其它处理：发送到日志系统、监控等）
        System.out.println("[SpEL日志] " + result);
        // 6. 继续执行原方法
        return jointPoint.proceed();

    }
}

