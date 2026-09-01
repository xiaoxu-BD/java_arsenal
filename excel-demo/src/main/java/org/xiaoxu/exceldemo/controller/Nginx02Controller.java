package org.xiaoxu.exceldemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class Nginx02Controller {


    @GetMapping("/nginx02")
    public String sayNginx(String name){
        return "nginx02" + name;
    }

    @GetMapping("/nginx02/headers")
    public Map<String, String> headers(@RequestHeader Map<String, String> headers) {
        return headers;
    }
}
