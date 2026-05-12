package org.xiaoxu.channel.sign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 默认验签器 — 骨架实现，按渠道扩展实际签名算法
 *
 * 扩展方式：
 * 1. 新建 AlipaySignVerifier implements ChannelSignVerifier，加 @Component
 * 2. 注入 Map<String, ChannelSignVerifier>，按 channelCode 路由
 */
@Slf4j
@Component
public class DefaultChannelSignVerifier implements ChannelSignVerifier {

    @Override
    public boolean verify(String channelCode, Map<String, String> headers, String body) {
        // TODO: 对接各渠道实际签名算法
        // 示例：Alipay — RSA2验签 / SF — HMAC-SHA256 / 银联 — 证书验签
        log.info("【验签】channelCode={}, 暂通过(待实现具体算法)", channelCode);
        return true;
    }
}
