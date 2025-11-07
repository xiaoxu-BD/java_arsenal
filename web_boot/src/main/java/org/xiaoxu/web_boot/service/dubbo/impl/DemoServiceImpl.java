package org.xiaoxu.web_boot.service.dubbo.impl;

import org.apache.dubbo.config.annotation.DubboService;
import org.xiaoxu.demo.DemoService;

/**
 * @className: DemoServiceImpl
 * @author: xiaoxu
 * @date: 2025/11/6 13:28
 * @Version: 1.0
 * @description: 声明服务提供者
 */
@DubboService(interfaceClass = DemoService.class, version = "1.0.0", group = "studyRPC")
public class DemoServiceImpl implements DemoService {
    @Override
    public String isDemo(String dds) {
        return  dds + "i M demo hope you can success";
    }
}
