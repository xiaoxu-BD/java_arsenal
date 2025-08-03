package org.xiaoxu.service.impl;

import org.xiaoxu.service.HelloService;

/**
 * @className: HelloServiceImpl
 * @author: xiaoxu
 * @date: 2025/7/29 21:28
 * @Version: 1.0
 * @description:
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "hello " + name;
    }
}
