package org.xiaoxu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.service.UserPointService;
import org.xiaoxu.utils.Result;

@RestController
@RequestMapping("/sign")
public class SignInController {

    @Autowired
    private UserPointService userPointService;

    /**
     * 签到
     */
    @PostMapping("/{userId}")
    public Result<?> signIn(@PathVariable Long userId) {
        userPointService.signIn(userId);
        return Result.success("签到成功");
    }

    /**
     * 查询今日是否已签到
     */
    @GetMapping("/{userId}/status")
    public Result<?> signStatus(@PathVariable Long userId) {
        Boolean signed = userPointService.hasSignedToday(userId);
        return Result.success(signed);
    }
}
