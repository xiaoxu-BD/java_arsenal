package org.xiaoxu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.common.Result;
import org.xiaoxu.entity.User;

/**
 * @className: BuilderController
 * @author: xiaoxu
 * @date: 2025/9/2 22:13
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/builder")
public class BuilderController {

    @GetMapping("toUser")
    public Result builderUser(){
        User user = User.builder().id(1L).email("tingqq7z@163.com").age(22).name("tingq").build();
        return Result.success(user);
    }
}
