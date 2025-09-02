package org.xiaoxu.web_boot.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.web_boot.aop.ParamCheck;
import org.xiaoxu.web_boot.common.Result;
import org.xiaoxu.web_boot.exception.BizException;

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


    @GetMapping("aop")
    @ParamCheck(ignore = true)
    public Result<?> aopTest(@RequestParam(value = "str",required = false) String str , @RequestParam(value = "str2",required = false) String str2){
        if (StringUtils.equalsIgnoreCase(str,str2)){
            throw new BizException("内容不能相等");
        }
        return Result.success("内容不相等");
    }



}
