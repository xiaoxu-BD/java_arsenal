package org.xiaoxu.domain.channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xiaoxu.enums.PayMethod;
import org.xiaoxu.exception.BizException;
import org.xiaoxu.service.PayChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// channel/PayChannelFactory.java
@Component
public class PayChannelFactory {

    private final Map<PayMethod, PayChannel> channelMap = new HashMap<>();

    /**
     * Spring 自动注入所有 PayChannel 实现类
     * 构造时按 supportMethod() 建立映射
     */
    @Autowired
    public PayChannelFactory(List<PayChannel> channels) {
        for (PayChannel channel : channels) {
            channelMap.put(channel.supportMethod(), channel);
        }
        log.info("已注册支付渠道: {}", channelMap.keySet());
    }

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(PayChannelFactory.class);

    /** 根据支付方式获取对应渠道 */
    public PayChannel getChannel(PayMethod method) {
        PayChannel channel = channelMap.get(method);
        if (channel == null) {
            throw new BizException("不支持的支付方式: " + method);
        }
        return channel;
    }
}