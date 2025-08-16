package org.xiaoxu.web_boot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.web_boot.common.Result;
import org.xiaoxu.web_boot.entity.es.User;
import org.xiaoxu.web_boot.service.es.UserService;

import java.io.IOException;

/**
 * @className: ESController
 * @author: xiaoxu
 * @date: 2025/8/16 10:43
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/es")
@RequiredArgsConstructor
public class ESController {
    private final UserService userService;



    @PostMapping("saveOrUpdate")
    public Result<?> saveOrUpdateEs(@RequestBody User userEs) throws IOException {
        userService.searchDoc(userEs);
        return Result.success("保存/更新成功");
    }

    @GetMapping("search")
    public Result<?> searchEs(String name) throws IOException {
        userService.searchUser(name);
        return Result.success("查询成功");
    }

}
