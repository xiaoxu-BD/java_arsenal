package org.xiaoxu.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @className: MyInvoication
 * @author: xiaoxu
 * @date: 2025/7/29 21:29
 * @Version: 1.0
 * @description:
 */
public class MyInvocation implements InvocationHandler {

    private Object target;

    public MyInvocation(Object target) {
        this.target = target;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Before method call: " + method.getName());
        Object result = method.invoke(target, args);  // 调用目标方法
        System.out.println("After method call: " + method.getName());
        return result;
    }

    public Object   getNewInstance() {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), target.getClass().getInterfaces(), new MyInvocation(target));
    }
}
