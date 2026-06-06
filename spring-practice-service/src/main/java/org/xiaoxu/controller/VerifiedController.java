package org.xiaoxu.controller;

//import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.aop.OpLog;
import org.xiaoxu.pojo.request.LoginRequest;
import org.xiaoxu.utils.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.xiaoxu.component.SafeCounter;
import org.xiaoxu.component.UnsafeCounter;
import org.xiaoxu.factory.HttpClientFactoryBean;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

/**
 * @className: VerifiedController
 * @author: xiaoxu
 * @date: 2025/11/19 15:55
 * @Version: 1.0
 * @description:
 */
@RestController
@Slf4j
public class VerifiedController {

    @Autowired
    private UnsafeCounter unsafeCounter;

    @Autowired
    private SafeCounter safeCounter;

    @Autowired
    private ApplicationContext context;

    @GetMapping("/test")
    @OpLog(scene = "真的在测试")
    public String test(){
        return "String type can be cast !";
    }

    @GetMapping("/thread/unsafe")
    public Map<String, Object> testUnsafe() throws InterruptedException {
        unsafeCounter.reset();

        // 10个线程同时自增，每个线程自增10次，期望最终结果是100
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    unsafeCounter.increment();
                }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        Map<String, Object> result = new HashMap<>();
        result.put("期望值", 100);
        result.put("实际值", unsafeCounter.getCount());
        result.put("是否线程安全", unsafeCounter.getCount() == 100);
        return result;
    }

    @GetMapping("/thread/safe")
    public Map<String, Object> testSafe() throws InterruptedException {
        safeCounter.reset();

        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    safeCounter.increment();
                }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        Map<String, Object> result = new HashMap<>();
        result.put("期望值", 100);
        result.put("实际值", safeCounter.getCount());
        result.put("是否线程安全", safeCounter.getCount() == 100);
        return result;
    }

    @GetMapping("/factory-bean")
    public Map<String, Object> testFactoryBean() {
        Map<String, Object> result = new HashMap<>();

        // 不加 & → 拿到 FactoryBean 生产的对象（HttpClient）
        HttpClient client = context.getBean("myHttpClient", HttpClient.class);
        result.put("不加&前缀", client.getClass().getName());

        // 加 & → 拿到 FactoryBean 本身（HttpClientFactoryBean）
        Object factory = context.getBean("&myHttpClient");
        result.put("加&前缀", factory.getClass().getName());

        // 直接注入的也是 FactoryBean 生产的对象
        result.put("直接@Autowired", context.getBean(HttpClient.class).getClass().getName());

        return result;
    }

    @XxlJob("demoHandler")
    @OpLog(scene = "xxl-job")
    public void demoHandler() {
        log.info("xxl-job is ready ");
    }
    @OpLog(scene = "no condition test RequestBody")
    @PostMapping("/auth/login")
    public Result<?> auth(@RequestBody LoginRequest loginRequest){
       Map<String, String> map = new HashMap<>();
       map.put("userName",loginRequest.getUserName());
       map.put("password",loginRequest.getPassword());
       return Result.success(map);

    }
}
