package org.xiaoxu.nacos;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @className: NacosTest
 * @author: xiaoxu
 * @date: 2025/6/2 21:11
 * @Version: 1.0
 * @description:
 */
@Slf4j
public class NacosTest  extends TestCase {

    public void testNacos1(){

        Map<String, Map<String, Service>> registry = new HashMap<>();

        // 初始化数据：两个命名空间（public, dev）
        Map<String, Service> publicServices = new HashMap<>();
        Map<String, Service> devServices = new HashMap<>();

        // 创建服务和实例
        Service userService = new Service("user-service");
        userService.addInstance(new Instance("192.168.1.1", 8080));
        userService.addInstance(new Instance("192.168.1.2", 8081));

        Service orderService = new Service("order-service");
        orderService.addInstance(new Instance("192.168.2.1", 8080));

        Service paymentService = new Service("payment-service");
        paymentService.addInstance(new Instance("192.168.3.1", 9090));

        // 填充数据
        publicServices.put("user-service", userService);
        publicServices.put("order-service", orderService);
        devServices.put("payment-service", paymentService);

        registry.put("public", publicServices);
        registry.put("dev", devServices);


        System.out.println("=== 方式 1：使用 forEach 循环 ===");
        registry.forEach((namespace, serviceMap) -> {
            System.out.println("Namespace: " + namespace);
            serviceMap.forEach((serviceName, service) -> {
                System.out.println("  Service: " + serviceName);
                service.getInstances().forEach(instance ->
                        System.out.println("    Instance: " + instance)
                );
            });
        });

        log.info("第二种遍历方式增强for");
        for (Map.Entry<String, Map<String, Service>> mapEntry : registry.entrySet()) {
            String nameSpace = mapEntry.getKey();
            System.out.println("Namespace: " + nameSpace);

            Map<String, Service> value = mapEntry.getValue();
            for (Map.Entry<String, Service> entry : value.entrySet()) {
                String serviceName = entry.getKey();
                 System.out.println("  Service: " + serviceName);
                 entry.getValue().getInstances().forEach(instance ->
                         System.out.println("    Instance: " + instance)
                 );
            }
        }
    }

}
