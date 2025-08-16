package org.xiaoxu.web_boot.controller;

import cn.hutool.extra.spring.SpringUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.web_boot.common.Result;
import org.xiaoxu.web_boot.entity.ItoOrderRpaReturnOrderThird;
import org.xiaoxu.web_boot.service.AddressService;
import org.xiaoxu.web_boot.service.PersonService;
import org.xiaoxu.web_boot.service.ThirdService;

import java.util.List;

/**
 * @className: DataController
 * @author: xiaoxu
 * @date: 2025/7/14 21:30
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/data")
public class DataController {
    private static final Logger log = LoggerFactory.getLogger(DataController.class);
    @Autowired
    private ThirdService thirdService;

    @Autowired
    private PersonService personService;

    @GetMapping("/get")
    public Result<?> getKey(){
        List<ItoOrderRpaReturnOrderThird> onCondition = thirdService.getOnCondition();
        return Result.success(thirdService.getValue(onCondition));
    }

    @Operation(summary = "使用PageHelper分页查询")
    @GetMapping("/page")
    public Result<?> getPage(@RequestParam(value = "pageNo") int pageNo, @RequestParam(value = "pageSize") int pageSize){
        return Result.success(thirdService.getPage(pageNo, pageSize));
    }


    @Operation(summary = "使用PageHelper一步分页查询完毕转换成PageResult")
    @GetMapping("/page/result")
    public Result<?> getPageResult(@RequestParam(value = "pageNo") int pageNo, @RequestParam(value = "pageSize") int pageSize){
        return Result.success(thirdService.getPageResult(pageNo, pageSize));
    }

    @Operation(summary = "使用PageHelper分页带参数查询")
    @GetMapping("/pageOnCondition")
    public Result<?> getPage(@RequestParam String id, @RequestParam String name,
                             @RequestParam int pageNo, @RequestParam int pageSize){
        return Result.success(thirdService.getPageByCondition(id,name,pageNo, pageSize));
    }


    @Operation(summary = "resultMap联系测试")
    @GetMapping("/resultMap")
    public Result<?> getResultMap(@RequestParam Long id){
        return Result.success(personService.getPersonInfo(id));
    }

    @Operation(summary = "测试xml中批量更新")
    @GetMapping("updateBatch")
    public Result<?> getUpdateBatch(){
        personService.updateBatch();
        return Result.success();
    }

    @Operation(summary = "测试mp分页查询 要写分页插件")
    @GetMapping("getPersonPage")
    public Result<?> getPersonPage(int pageNo, int pageSize){
        return Result.success(personService.getPersonPage(pageNo, pageSize));
    }

    @Operation(summary = "测试xml中动态sql IF")
    @GetMapping("getPersonByName")
    public Result<?> getPersonByName(String name){
        ///用来获取spring上下文中的bean
//        SpringUtil.getBean()
        return Result.success(personService.getPersonByName(name));
    }

}
