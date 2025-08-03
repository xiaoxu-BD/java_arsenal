package org.xiaoxu.web_boot.service.dubbo.impl;

//import org.apache.dubbo.config.annotation.DubboService;
//import org.xiaoxu.web_boot.service.dubbo.HelloService;

import org.xiaoxu.web_boot.service.dubbo.HelloService;

/**
 * @className: HelloServiceImpl
 * @author: xiaoxu
 * @date: 2025/7/25 19:58
 * @Version: 1.0
 * @description:
 */
//@DubboService //	用于服务提供者，发布服务
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "hello " + name;
    }
}
