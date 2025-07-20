package org.xiaoxu.web_boot.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.xiaoxu.web_boot.config.datasource.DynamicDataSourceContextHolder;

import java.lang.reflect.Method;

@Aspect
@Order(-1) // 保证在事务之前执行
@Component
public class DynamicDataSourceAspect {
        @Pointcut("@annotation(org.xiaoxu.web_boot.aop.DS)")
        public void dsPointCut() {
        }

        @Before("dsPointCut()")
        public void beforeSwitchDS(JoinPoint point) {
            try {
                // 获取方法上的 @DS 注解
                Method method = ((MethodSignature) point.getSignature()).getMethod();
                DS ds = method.getAnnotation(DS.class);

                // 如果方法上没有 @DS 注解，则尝试获取类上的 @DS 注解
                if (ds == null) {
                    ds = point.getTarget().getClass().getAnnotation(DS.class);
                }

                // 如果找到 @DS 注解，则切换数据源
                if (ds != null) {
                    String dataSourceKey = ds.value();
                    DynamicDataSourceContextHolder.setDataSourceKey(dataSourceKey);
                }
            } catch (Exception e) {
                System.err.println("Failed to switch data source: " + e.getMessage());
            }
        }


        @After("dsPointCut()")
        public void clearDS() {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }


    }