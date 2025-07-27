package org.xiaoxu.web_boot.controller;

/**
 * @className: DubboController
 * @author: xiaoxu
 * @date: 2025/7/25 19:56
 * @Version: 1.0
 * @description:
 */

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.web_boot.service.dubbo.HelloService;

@RestController
@RequestMapping("/dubbo")
public class DubboController {
//	用于消费者，引用远程服务
    @DubboReference
    private HelloService helloService;

    @GetMapping("/hello")
    public String hello(@RequestParam("name") String name) {
        return helloService.sayHello(name);
    }
}
