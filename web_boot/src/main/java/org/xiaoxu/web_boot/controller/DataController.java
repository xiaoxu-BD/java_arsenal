package org.xiaoxu.web_boot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.web_boot.common.Result;
import org.xiaoxu.web_boot.entity.ItoOrderRpaReturnOrderThird;
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

    @GetMapping("/get")
    public Result<?> getKey(){
        List<ItoOrderRpaReturnOrderThird> onCondition = thirdService.getOnCondition();
        return Result.success(thirdService.getValue(onCondition));
    }
}
