package org.xiaoxu.se.apispi;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * API vs SPI 核心区别：
 *
 *   API (Application Programming Interface)
 *     -> 框架/库 定义接口，使用者（你）来调用
 *     -> 控制权在 调用方
 *     -> 例子：List.add()、JDBC Connection、Spring BeanFactory.getBean()
 *
 *   SPI (Service Provider Interface)
 *     -> 框架/库 定义接口，第三方 来实现
 *     -> 控制权在 框架（通过 ServiceLoader 发现并加载实现）
 *     -> 例子：JDBC Driver、SLF4J LoggerFactory、Dubbo ExtensionLoader
 *
 *   SPI 的 Java 标准实现：
 *     1. 定义接口 (如 LoggerSpi)
 *     2. 第三方提供实现 (如 ConsoleLoggerSpi, FileLoggerSpi)
 *     3. 在 META-INF/services/ 下创建以接口全限定名命名的文件
 *     4. 文件内容为实现类的全限定名（每行一个）
 *     5. 使用 java.util.ServiceLoader.load(接口.class) 自动发现并加载
 */
public class APIAndSPI {

    // ================================================================
    //  一、API 演示 —— 你调用别人定义好的接口
    // ================================================================
    static class ApiExample {
        // 这就是 API：你作为使用者，调用 List 提供的方法
        static void run() {
            System.out.println("====== API 演示: 你调用框架定义的方法 ======");
            List<String> list = new ArrayList<>();
            list.add("A");          // 你调用 API
            list.add("B");
            list.remove(0);         // 你调用 API
            System.out.println("API 调用结果: " + list);

            // 关键：控制权在你手里，你决定什么时候、调用哪个方法
            System.out.println("-> 你主动调用，控制权在调用方\n");
        }
    }

    // ================================================================
    //  二、SPI 演示 —— 框架发现并加载你提供的实现
    // ================================================================
    static class SpiExample {
        static void run() {
            System.out.println("====== SPI 演示: 框架自动发现你的实现 ======");

            // ServiceLoader 自动扫描 META-INF/services/org.xiaoxu.se.apispi.LoggerSpi
            // 该文件中列出了所有实现类 ：：  在 META-INF/services/ 下注册实现类全限定名
            ServiceLoader<LoggerSpi> loggers = ServiceLoader.load(LoggerSpi.class);

            System.out.println("发现的 SPI 实现:");
            for (LoggerSpi logger : loggers) {
                System.out.println("  -> " + logger.getClass().getSimpleName()
                        + " (type=" + logger.type() + ")");
            }

            System.out.println("\n调用所有发现的实现:");
            for (LoggerSpi logger : loggers) {
                logger.log("Hello from SPI!");
            }

            System.out.println("-> 框架自动发现并加载，控制权在框架方\n");
        }
    }

    // ================================================================
    //  三、模拟 SPI 的实际业务场景 —— 支付渠道
    // ================================================================
    // 框架定义支付接口
    interface PayChannel {
        String name();
        void pay(double amount);
    }

    // 业务方A：实现支付宝支付
    static class AlipayChannel implements PayChannel {
        @Override
        public String name() { return "支付宝"; }

        @Override
        public void pay(double amount) {
            System.out.println("[支付宝] 支付 ¥" + amount);
        }
    }

    // 业务方B：实现微信支付
    static class WechatPayChannel implements PayChannel {
        @Override
        public String name() { return "微信支付"; }

        @Override
        public void pay(double amount) {
            System.out.println("[微信支付] 支付 ¥" + amount);
        }
    }

    // 支付网关：框架端，通过 SPI 加载所有支付渠道，用户选哪个就用哪个
    static class PaymentGateway {
        private final List<PayChannel> channels = new ArrayList<>();

        // 模拟 ServiceLoader 的行为：扫描实现类并注册
        PaymentGateway() {
            // 真实场景中这里用 ServiceLoader.load(PayChannel.class)
            // 这里手动注册来演示框架加载的逻辑
            channels.add(new AlipayChannel());
            channels.add(new WechatPayChannel());
        }

        void pay(String channelName, double amount) {
            for (PayChannel ch : channels) {
                if (ch.name().equals(channelName)) {
                    ch.pay(amount);
                    return;
                }
            }
            System.out.println("不支持的支付方式: " + channelName);
        }

        void listChannels() {
            System.out.println("支持的支付渠道:");
            channels.forEach(ch -> System.out.println("  - " + ch.name()));
        }
    }

    static void businessScenario() {
        System.out.println("====== 业务场景: 支付网关 SPI ======");
        PaymentGateway gateway = new PaymentGateway();
        gateway.listChannels();
        gateway.pay("支付宝", 99.9);
        gateway.pay("微信支付", 50.0);
        System.out.println("-> 新增支付方式只需添加实现，网关代码无需改动\n");
    }

    // ================================================================
    //  main
    // ================================================================
    public static void main(String[] args) {
 /*       // 1. API: 你调用别人
        ApiExample.run();*/

        // 2. SPI: 框架发现你的实现 (真正的 ServiceLoader)
        SpiExample.run();

/*        // 3. 业务场景模拟
        businessScenario();*/

        // 4. 总结
        System.out.println("========================================");
        System.out.println("  API vs SPI 本质区别");
        System.out.println("========================================");
        System.out.println("API: \"我定义方法，你来调\"  → 控制权在调用方");
        System.out.println("SPI: \"我定义接口，你实现\"  → 控制权在框架方");
        System.out.println();
        System.out.println("经典 SPI 案例:");
        System.out.println("  JDBC Driver:  java.sql.Driver 接口，各数据库厂商实现");
        System.out.println("  SLF4J:        LoggerFactory 加载不同日志实现");
        System.out.println("  Spring:       SpringFactoriesLoader (SpringBoot)");
        System.out.println("  Dubbo:        ExtensionLoader 扩展点加载");
    }
}
