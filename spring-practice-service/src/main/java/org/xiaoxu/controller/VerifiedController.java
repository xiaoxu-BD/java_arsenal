package org.xiaoxu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.aop.OpLog;

/**
 * @className: VerifiedController
 * @author: xiaoxu
 * @date: 2025/11/19 15:55
 * @Version: 1.0
 * @description:
 */
@RestController
public class VerifiedController {

    @GetMapping("/test")
    @OpLog(scene = "真的在测试")
    public String test(){
        return "String type can be cast !";
    }
}
