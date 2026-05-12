package org.xiaoxu.channel.sign;

import java.util.Map;

/**
 * 渠道回调签名验证器
 * 各渠道实现此接口，完成签名算法对接
 */
public interface ChannelSignVerifier {

    /**
     * 验证回调签名
     * @param channelCode 渠道编码
     * @param headers 请求头
     * @param body 请求体
     * @return true=验签通过
     */
    boolean verify(String channelCode, Map<String, String> headers, String body);
}
