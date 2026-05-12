package org.xiaoxu.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.service.SendCodeService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class VerificationCodeController {

    private final SendCodeService sendCodeService;

    /**
     * 异步确认模式 - 发送验证码
     *
     * 流程：
     * 1. 客户端请求发送验证码
     * 2. 服务端立即返回"验证码发送中"（不阻塞）
     * 3. 后台线程模拟厂商接口（~5s返回4位数字）
     * 4. 厂商返回后，将结果发到 RabbitMQ
     * 5. MQ消费者收到通知，完成后续处理（如缓存验证码）
     */
    @PostMapping("/send-code")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestParam String phone) {
        sendCodeService.asyncSendCode(phone);
        return ResponseEntity.ok(Map.of(
                "phone", phone,
                "message", "验证码正在发送中，请稍后查收"
        ));
    }
}
