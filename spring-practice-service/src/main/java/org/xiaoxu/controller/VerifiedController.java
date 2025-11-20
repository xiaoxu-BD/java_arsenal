package org.xiaoxu.controller;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.aop.OpLog;

/**
 * @className: VerifiedController
 * @author: xiaoxu
 * @date: 2025/11/19 15:55
 * @Version: 1.0
 * @description:
 */
@RestController
@Slf4j
public class VerifiedController {

    @GetMapping("/test")
    @OpLog(scene = "真的在测试")
    public String test(){
        return "String type can be cast !";
    }

    @XxlJob("demoHandler")
    @OpLog(scene = "xxl-job")
    public void demoHandler() {
        log.info("xxl-job is ready ");
    }
}
