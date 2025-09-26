package org.xiaoxu;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

/**
 * @className: SpringCloudTest
 * @author: xiaoxu
 * @date: 2025/9/25 21:40
 * @Version: 1.0
 * @description:
 */
@SpringBootTest
public class SpringCloudTest {




    @Autowired
    DiscoveryClient discoveryClient;




    @Test
    public void testInstances(){
        List<String> services = discoveryClient.getServices();
        for (String service : services) {
            System.out.println("服务名称：" + service);


            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            for (ServiceInstance instance : instances) {
                System.out.println("服务实例：" + instance.getHost());
            }
        }



    }
}
