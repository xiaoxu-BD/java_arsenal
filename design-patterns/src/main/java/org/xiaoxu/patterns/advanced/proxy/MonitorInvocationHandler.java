package org.xiaoxu.patterns.advanced.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 监控处理器（高级版代理：JDK 动态代理）
 *
 * <p>和 basic 静态代理的关键区别：这里没有为 UserService 手写代理类，
 * {@code Proxy.newProxyInstance} 在<b>运行期</b>动态生成代理类，
 * 一个 handler 可以服务任意接口——这正是 Spring AOP、MyBatis Mapper、
 * RPC 客户端 stub 的底层原理。
 *
 * <p>限制：JDK 动态代理只能代理接口。目标类没有接口时用 CGLIB（子类字节码增强）。
 */
public class MonitorInvocationHandler implements InvocationHandler {

    /** 真实目标：Object 类型，不绑定任何具体接口——这就是"动态"通用性的来源 */
    private final Object target;

    public MonitorInvocationHandler(Object target) {
        this.target = target;
    }

    /**
     * 代理方法：所有对代理对象的方法调用最终都落到这里
     *
     * @param proxy  JDK 生成的代理对象（注意：不是目标对象，谨慎使用）
     * @param method 被调用的方法元信息
     * @param args   实参
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = target.getClass().getSimpleName() + "." + method.getName();
        System.out.println("[MONITOR] -> " + methodName + " args=" + java.util.Arrays.toString(args));

        long start = System.nanoTime();
        try {
            Object result = method.invoke(target, args);          // 反射调用真实目标
            System.out.println("[MONITOR] <- " + methodName
                    + " 耗时=" + (System.nanoTime() - start) / 1000 + "μs result=" + result);
            return result;
        } catch (java.lang.reflect.InvocationTargetException e) {
            // method.invoke 的业务异常都包在 InvocationTargetException 里，拆开继续抛给调用方
            System.out.println("[MONITOR] !! " + methodName + " 抛出: "
                    + e.getCause().getClass().getSimpleName());
            throw e.getCause();
        }
    }

    /** 通用工厂：任意接口 + 任意实现，都能包出带监控能力的代理 */
    @SuppressWarnings("unchecked")
    public static <T> T wrap(T target, Class<T> iface) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                new MonitorInvocationHandler(target));
    }
}
