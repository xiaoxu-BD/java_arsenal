package org.xiaoxu.service;

import org.xiaoxu.domain.channel.ChannelResult;
import org.xiaoxu.domain.entity.PaymentOrder;
import org.xiaoxu.enums.PayMethod;

public interface PayChannel {

    /** 标识该渠道支持哪种支付方式 */
    PayMethod supportMethod();

    /** 发起支付，返回渠道统一结果 */
    ChannelResult pay(PaymentOrder order);
}