package org.xiaoxu.service;

import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class GreetingServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "Hello, " + name + "! from Dubbo provider.";
    }
}