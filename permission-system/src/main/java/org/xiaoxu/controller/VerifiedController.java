package org.xiaoxu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.common.utils.Result;

/**
 * @className: VerifiedController
 * @author: xiaoxu
 * @date: 2025/11/18 8:48
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/test")
public class VerifiedController {


    @GetMapping("/verified")
    public Result<?> test() {
        return Result.success("验证!");
    }
}
