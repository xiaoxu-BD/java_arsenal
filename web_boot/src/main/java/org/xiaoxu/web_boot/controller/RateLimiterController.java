package org.xiaoxu.web_boot.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.web_boot.service.LeakyBucketService;

/**
 * @className: RateLimiterController
 * @author: xiaoxu
 * @date: 2025/10/30 7:21
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping
public class RateLimiterController {
    @Resource
    private LeakyBucketService leakyBucketService;

    @GetMapping("/limit")
    public String limit(HttpServletRequest request) throws InterruptedException {
        leakyBucketService.limit(request);
        return "SUCCESS";
    }


}
