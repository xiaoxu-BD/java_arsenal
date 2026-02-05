package org.xiaoxu.web_boot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.web_boot.aop.lock.DistributeLock;

@RestController
@RequestMapping("/lock")
public class LockTestController {

    @GetMapping("/test")
    @DistributeLock(scene = "test", keyExpression = "#id", expireTime = 3000)
    public String test(@RequestParam Long id) throws InterruptedException {
        System.out.println("进入方法，线程：" + Thread.currentThread().getName());
        Thread.sleep(20000);
        System.out.println("离开方法，线程：" + Thread.currentThread().getName());
        return "ok";
    }
}
