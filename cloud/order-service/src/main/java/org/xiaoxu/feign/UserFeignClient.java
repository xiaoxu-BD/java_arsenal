package org.xiaoxu.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @className: UserFeignClient
 * @author: xiaoxu
 * @date: 2025/10/8 17:49
 * @Version: 1.0
 * @description:
 */
@FeignClient(value = "server-user")
public interface UserFeignClient {



    @GetMapping("/api/user/me")
    String getMyInfo();
}
