package org.xiaoxu.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文持有者，用于在非 Spring 管理的对象中获取 Bean。
 * <p>
 * 典型场景：Flowable ExecutionListener 由引擎实例化，无法直接使用 @Resource/@Autowired，
 * 通过本类的静态方法 {@link #getBean(Class)} 获取所需的 Spring Bean。
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }
}
