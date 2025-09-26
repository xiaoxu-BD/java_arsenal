package org.xiaoxu.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.utils.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @className: ConfigController
 * @author: xiaoxu
 * @date: 2025/9/24 19:37
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/config")
public class ConfigController {
    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("/get")
    public String get() {
        return "appName: " + appName + " appVersion: " + appVersion;
    }




}
