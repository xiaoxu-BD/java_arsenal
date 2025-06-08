package org.xiaoxu.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @className: UserController
 * @author: xiaoxu
 * @date: 2025/6/4 22:39
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/user")
public class UserController {


    @GetMapping("/hi")
    public String hi(HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        // 获取服务器接收此请求的端口号
        int serverPort = request.getServerPort();

        System.out.println("PATH: " + requestUri);
        System.out.println("PORT: " + serverPort); // 在这里打印端口号

        return "Spring Cloud Gateway!";
    }

}
