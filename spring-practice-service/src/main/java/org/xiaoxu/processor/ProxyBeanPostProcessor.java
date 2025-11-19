package org.xiaoxu.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.xiaoxu.component.TaskService2;

import java.lang.reflect.Proxy;

/**
 * @className: ProxyBeanPostProcessor
 * @author: xiaoxu
 * @date: 2025/11/19 14:50
 * @Version: 1.0
 * @description:
 */
@Component
public class ProxyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TaskService2){

            System.out.println("[代理处理器]正在"  + beanName + "创建代理对象...");
            return Proxy.newProxyInstance(
                    bean.getClass().getClassLoader(),
                    bean.getClass().getInterfaces(),
                    (proxy, method, args) -> {
                        System.out.println("[代理处理器]正在调用" + method.getName() + "的方法...");
                        Object result = method.invoke(bean, args);
                        System.out.println("[代理处理器]" + method.getName() + "的方法调用完毕...");

                        return result;
                    }
            );
        }

        return bean;
    }
}
