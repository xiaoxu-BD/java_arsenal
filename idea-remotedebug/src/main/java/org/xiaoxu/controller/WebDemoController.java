package org.xiaoxu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @className: WebDemoController
 * @author: xiaoxu
 * @date: 2026/4/4 10:26
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/api")
public class WebDemoController {


    @GetMapping("/run/{name}")
    public String remoteDebug(@PathVariable(value = "name") String name){
        return "hello " + name;
    }
}
