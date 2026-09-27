package org.xiaoxu.patterns.advanced.facade;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 下单门面（高级版 Facade）
 *
 * <p>客户端一个 {@link #placeOrder} 完成下单，不必知道背后编排了几个子系统。
 * 门面除了"收口"，还要承担工程上的三件事：
 * <ol>
 *   <li><b>编排顺序</b>：先扣库存再支付，顺序错了就是资损；</li>
 *   <li><b>异常语义</b>：子系统异常翻译成业务异常（下单失败原因），不把技术细节漏给调用方；</li>
 *   <li><b>扩展点</b>：发优惠券、风控等新步骤往这里加，调用方无感。</li>
 * </ol>
 *
 * <p>真实项目里的同款：应用层 AppService / RPC 服务门面。
 * （跨库事务实际用 Saga/TCC 处理，本示例只演示编排职责。）
 */
public class OrderFacade {

    private final InventoryService inventory = new InventoryService();
    private final PaymentService payment = new PaymentService();
    private final LogisticsService logistics = new LogisticsService();

    private final AtomicLong orderSeq = new AtomicLong(2026090900L);

    /** 下单一键完成，返回订单号 */
    public String placeOrder(String sku, int count, BigDecimal amount, String address) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于 0");
        }
        String orderNo = "SO-" + orderSeq.incrementAndGet();

        inventory.deductStock(sku, count);   // 1. 扣库存
        payment.charge(orderNo, amount);     // 2. 支付
        logistics.arrangeShipment(orderNo, address);   // 3. 发货
        return orderNo;
    }
}
