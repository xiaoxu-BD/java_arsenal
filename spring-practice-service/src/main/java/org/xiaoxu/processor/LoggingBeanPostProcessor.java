package org.xiaoxu.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @className: LoggingBeanPostProcessor
 * @author: xiaoxu
 * @date: 2025/11/19 9:23
 * @Version: 1.0
 * @description:
 */
//@Component
public class LoggingBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("[前制处理] Bean : " + beanName + " 开始初始化");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("[后置处理] Bean :" + beanName + " 初始化完成");
        return bean;
    }
}
