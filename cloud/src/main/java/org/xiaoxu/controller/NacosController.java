package org.xiaoxu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @className: NacosController
 * @author: xiaoxu
 * @date: 2025/6/2 21:46
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping ("/nacos")
public class NacosController {


    @GetMapping("/test/{name}")
    public String sayHi(@PathVariable("name") String name) {
        return "Hi Nacos Discovery " + name;
    }
}
