package org.xiaoxu.web_boot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.web_boot.common.Result;
import org.xiaoxu.web_boot.entity.Address;
import org.xiaoxu.web_boot.service.AddressService;

/**
 * @className: RedisController
 * @author: xiaoxu
 * @date: 2025/8/10 8:36
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RedisController {
    private final AddressService addressService;



    @GetMapping("/getFromRedis")
    public Result<?> login(@RequestParam String key){
        return Result.success(addressService.getFromRedis(key));
    }


    @PostMapping("/setToRedis")
    public Result<?> setToRedis(@RequestBody Address address) {
    addressService.register(address);
    return Result.success("注册成功");
    }
}
