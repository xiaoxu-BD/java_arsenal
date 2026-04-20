package org.xiaoxu.controller;

import cn.hutool.core.bean.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.pojo.dto.SystemUserDTO;
import org.xiaoxu.service.SysUserService;

import java.util.List;

/**
 * @className: CommonController
 * @author: xiaoxu
 * @date: 2026/3/29 12:21
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/api/common")
public class CommonController {




    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/list")
    public Result<List<SystemUserDTO>> getList(){
        List<SystemUsers> userList = sysUserService.getAll();
        List<SystemUserDTO> userResultList = BeanUtil.copyToList(userList, SystemUserDTO.class);
        return Result.success(userResultList);
    }

}
