package org.xiaoxu.web_boot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @className: ConsumerController
 * @author: xiaoxu
 * @date: 2025/6/28 18:42
 * @Version: 1.0
 * @description: 使用nginx做反向代理
 */
@RestController
@RequestMapping("/")
public class ConsumerController {

    @GetMapping("hello1")
    public String hello() {
        return "hello";
    }



}
