package org.xiaoxu.designpattern.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 适配器模式 — 第三方系统接口（被适配者）
 *
 * 假设这是外部系统的接口，方法签名和返回格式与内部不一致：
 * - create(String goodsId, String qty, String token) → 返回 XML
 * - getStatus(String id) → 返回 code 数字
 *
 * 不能直接被业务代码使用，需要通过适配器桥接
 */
@Slf4j
@Component
public class ThirdPartyOrderApi {

    public String create(String goodsId, String qty, String token) {
        log.info("【第三方】创建订单: goodsId={}, qty={}, token={}", goodsId, qty, token);
        return "<result><code>0</code><msg>success</msg><orderNo>TP_" + goodsId + "</orderNo></result>";
    }

    public int getStatus(String id) {
        log.info("【第三方】查询状态: id={}", id);
        return 1; // 1=已支付, 2=已发货, 3=已完成
    }
}
