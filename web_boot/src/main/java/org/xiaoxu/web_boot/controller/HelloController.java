package org.xiaoxu.web_boot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class HelloController {

   private  static final Logger logger  = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/log")
    public String hello() {
        logger.info("处理请求");
        return "Hello, JavaArsenal!";
    }


}