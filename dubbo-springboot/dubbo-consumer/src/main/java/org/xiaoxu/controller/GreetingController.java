package org.xiaoxu.controller;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.service.HelloService;

@RestController
public class GreetingController {

    @DubboReference
    private HelloService helloService;

    @GetMapping("/hello")
    public String sayHello(@RequestParam String name) {
        return helloService.sayHello(name);
    }
}