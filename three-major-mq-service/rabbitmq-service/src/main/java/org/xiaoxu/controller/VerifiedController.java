package org.xiaoxu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @className: VerifiedController
 * @author: xiaoxu
 * @date: 2025/11/18 10:32
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/test")
public class VerifiedController {


    @GetMapping("/verified")
    public String test() {
        return "String type can be cast !";
    }
}
