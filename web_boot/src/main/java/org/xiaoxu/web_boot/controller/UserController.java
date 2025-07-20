package org.xiaoxu.web_boot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.web_boot.common.Result;
import org.xiaoxu.web_boot.common.request.RegisterParam;
import org.xiaoxu.web_boot.entity.vo.UserVO;
import org.xiaoxu.web_boot.service.entity.UserService;

import java.util.List;

/**
 * @className: UserController
 * @author: xiaoxu
 * @date: 2025/9/3 9:41
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;





    @PostMapping("/register")
    public Result<?> addUser(@Valid @RequestBody RegisterParam register){
        userService.addUser(register);
        return Result.success();
    }


    @GetMapping("/getUser")
    public Result<?> getUser(String idCard){
//        List<UserVO> voList  = userService.getUser();
      UserVO userVO =    userService.getByIdCard(idCard);

        return Result.success(userVO);
    }


    @PostMapping("/update")
    public Result<?> updateUser(@RequestBody UserVO userVO){
        userService.updateUser(userVO);
        return Result.success();
    }
}
