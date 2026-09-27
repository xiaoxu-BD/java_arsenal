package org.xiaoxu.patterns.advanced.proxy;

/**
 * JDK 动态代理演示（高级版）
 *
 * <p>调用方拿到的 proxy 和 UserServiceImpl 实现同一个接口，无感切换；
 * 日志与耗时统计被织入在方法前后，业务实现里一行监控代码都没有。
 */
public class JdkProxyDemo {

    public static void main(String[] args) {
        UserService raw = new UserServiceImpl();
        UserService proxied = MonitorInvocationHandler.wrap(raw, UserService.class);

        System.out.println("========== 1. 正常调用（经代理） ==========");
        proxied.getUserName(1001L);
        proxied.listPermissions(1001L);

        System.out.println("\n========== 2. 异常路径也被记录后原样抛出 ==========");
        try {
            proxied.getUserName(-1L);
        } catch (IllegalArgumentException e) {
            System.out.println("调用方收到业务异常: " + e.getMessage());
        }
    }
}
