package org.xiaoxu.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xiaoxu.domain.entity.PaymentOrder;
import org.xiaoxu.enums.PaymentStatus;
import org.xiaoxu.mapper.PaymentOrderMapper;

import java.time.LocalDateTime;
import java.util.List;

// scheduler/PaymentCompensationTask.java
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompensationTask {

    private final PaymentOrderMapper orderMapper;

    /** 每分钟扫描超时订单 */
    @Scheduled(fixedRate = 60_000)
    public void compensateTimeoutOrders() {

        LocalDateTime deadline = LocalDateTime.now().minusMinutes(5);

        List<PaymentOrder> stuckList = orderMapper.selectList(
            new LambdaQueryWrapper<PaymentOrder>()
                .in(PaymentOrder::getStatus, PaymentStatus.PENDING, PaymentStatus.PROCESSING)
                .lt(PaymentOrder::getCreateTime, deadline)
        );

        for (PaymentOrder order : stuckList) {
            log.warn("[补偿任务] 订单 {} 超时，自动关闭", order.getOrderId());
            orderMapper.update(null, new LambdaUpdateWrapper<PaymentOrder>()
                    .eq(PaymentOrder::getOrderId, order.getOrderId())
                    .set(PaymentOrder::getStatus, PaymentStatus.FAILED)
                    .set(PaymentOrder::getFailReason, "支付超时，系统自动关闭")
            );
        }
    }
}