package org.xiaoxu.se.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 动态代理复习：JDK Proxy vs CGLIB
 *
 * JDK Proxy：
 *   - 基于接口，目标类必须实现接口
 *   - 使用 java.lang.reflect.Proxy + InvocationHandler
 *   - 底层通过字节码生成代理类，实现目标接口
 *
 * CGLIB：
 *   - 基于继承，目标类可以没有接口
 *   - 使用 Enhancer + MethodInterceptor
 *   - 底层通过 ASM 字节码框架生成目标类的子类
 *   - 不能代理 final 类和 final 方法
 */
public class DynamicProxy {

    // ===================== 公共接口 =====================
    interface UserService {
        void add(String name);
        String findById(int id);
    }

    // ===================== 目标实现类 =====================
    static class UserServiceImpl implements UserService {
        @Override
        public void add(String name) {
            System.out.println("[Service] 添加用户: " + name);
        }

        @Override
        public String findById(int id) {
            System.out.println("[Service] 查询用户, id=" + id);
            return "User-" + id;
        }
    }

    // ===================== 无接口的目标类（CGLIB 专用）=====================
    static class OrderService {
        public void create(String orderNo) {
            System.out.println("[Service] 创建订单: " + orderNo);
        }

        public String query(int orderId) {
            System.out.println("[Service] 查询订单, id=" + orderId);
            return "Order-" + orderId;
        }
    }

    // ========================================================
    //  一、JDK 动态代理
    // ========================================================
    static class JdkProxyHandler implements InvocationHandler {
        private final Object target;

        public JdkProxyHandler(Object target) {
            this.target = target;
        }

        /**
         * 创建 JDK 代理实例
         * Proxy.newProxyInstance(类加载器, 目标类实现的接口数组, InvocationHandler)
         */
        @SuppressWarnings("unchecked")
        public <T> T getProxy() {
            return (T) Proxy.newProxyInstance(
                    target.getClass().getClassLoader(),
                    target.getClass().getInterfaces(),
                    this
            );
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            long start = System.currentTimeMillis();
            System.out.println("[JDK Proxy] 前置处理 -> " + method.getName());
            try {
                Object result = method.invoke(target, args);
                System.out.println("[JDK Proxy] 返回结果: " + result);
                return result;
            } catch (Exception e) {
                System.out.println("[JDK Proxy] 异常: " + e.getMessage());
                throw e;
            } finally {
                System.out.println("[JDK Proxy] 后置处理 <- " + method.getName()
                        + " 耗时: " + (System.currentTimeMillis() - start) + "ms");
            }
        }
    }

    // ========================================================
    //  二、CGLIB 动态代理
    // ========================================================
    static class CglibProxyFactory implements MethodInterceptor {
        private final Object target;

        public CglibProxyFactory(Object target) {
            this.target = target;
        }

        /**
         * 创建 CGLIB 代理实例
         * Enhancer 设置父类(目标类) + Callback(MethodInterceptor)
         */
        @SuppressWarnings("unchecked")
        public <T> T getProxy() {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(target.getClass());
            enhancer.setCallback(this);
            return (T) enhancer.create();
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            long start = System.currentTimeMillis();
            System.out.println("[CGLIB Proxy] 前置处理 -> " + method.getName());
            try {
                // proxy.invokeSuper: 调用父类(目标类)方法，不走代理
                // method.invoke(target): 也可以直接调用目标对象
                Object result = proxy.invokeSuper(obj, args);
                System.out.println("[CGLIB Proxy] 返回结果: " + result);
                return result;
            } catch (Exception e) {
                System.out.println("[CGLIB Proxy] 异常: " + e.getMessage());
                throw e;
            } finally {
                System.out.println("[CGLIB Proxy] 后置处理 <- " + method.getName()
                        + " 耗时: " + (System.currentTimeMillis() - start) + "ms");
            }
        }
    }

    // ========================================================
    //  三、验证代理类的继承关系
    // ========================================================
    static void printProxyClassInfo(Object proxy, String label) {
        System.out.println("------- " + label + " -------");
        System.out.println("代理对象类型: " + proxy.getClass().getName());
        System.out.println("是否是目标类的子类: " + (proxy instanceof UserServiceImpl));
        System.out.println("是否实现了接口: " + (proxy instanceof UserService));
        System.out.println("父类: " + proxy.getClass().getSuperclass().getName());
        Class<?>[] interfaces = proxy.getClass().getInterfaces();
        System.out.print("实现的接口: ");
        for (Class<?> i : interfaces) System.out.print(i.getSimpleName() + " ");
        System.out.println();
    }

    // ========================================================
    //  main
    //
    //  CGLIB 在 Java 17+ 下需要 VM options:
    //  --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED
    //  IntelliJ: Run → Edit Configurations → VM options 填入上述参数
    // ========================================================
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  一、JDK 动态代理 (基于接口)");
        System.out.println("========================================");

        UserServiceImpl realService = new UserServiceImpl();
        JdkProxyHandler jdkHandler = new JdkProxyHandler(realService);
        UserService jdkProxy = jdkHandler.getProxy();

        printProxyClassInfo(jdkProxy, "JDK Proxy");

        jdkProxy.add("张三");
        System.out.println();
        jdkProxy.findById(1);

        System.out.println();
        System.out.println("========================================");
        System.out.println("  二、CGLIB 动态代理 (基于继承)");
        System.out.println("========================================");

        // --- 对有接口的类也可以用 CGLIB ---
        CglibProxyFactory cglibFactory1 = new CglibProxyFactory(realService);
        UserService cglibProxyForImpl = cglibFactory1.getProxy();

        printProxyClassInfo(cglibProxyForImpl, "CGLIB Proxy (有接口的类)");

        cglibProxyForImpl.add("李四");
        System.out.println();
        cglibProxyForImpl.findById(2);

        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("  CGLIB 代理无接口的类");
        System.out.println("----------------------------------------");

        OrderService orderService = new OrderService();
        CglibProxyFactory cglibFactory2 = new CglibProxyFactory(orderService);
        OrderService cglibProxy = cglibFactory2.getProxy();

        printProxyClassInfo(cglibProxy, "CGLIB Proxy (无接口的类)");

        cglibProxy.create("ORD-001");
        System.out.println();
        cglibProxy.query(100);

        System.out.println();
        System.out.println("========================================");
        System.out.println("  三、核心区别总结");
        System.out.println("========================================");
        System.out.println("1. JDK Proxy: 目标类必须实现接口, proxy 也实现相同接口");
        System.out.println("2. CGLIB:     通过 ASM 生成目标类的子类, 不需要接口");
        System.out.println("3. JDK Proxy: InvocationHandler.invoke(proxy, method, args)");
        System.out.println("4. CGLIB:     MethodInterceptor.intercept(obj, method, args, proxy)");
        System.out.println("5. CGLIB 中用 proxy.invokeSuper(obj, args) 调用目标方法（避免循环）");
        System.out.println("6. JDK 中用 method.invoke(target, args) 调用目标方法");
        System.out.println("7. Spring AOP 默认策略: 有接口用 JDK Proxy, 无接口用 CGLIB");
        System.out.println("   (SpringBoot 2.0+ 默认统一使用 CGLIB)");
    }
}
