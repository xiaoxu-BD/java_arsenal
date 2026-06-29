package org.xiaoxu.pay.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xiaoxu.pay.service.impl.PayServiceImpl;

/**
 * 过期付款单清理定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayExpireJob {

    private final PayServiceImpl payService;

    @XxlJob("expirePendingOrders")
    public void execute() {
        log.info("开始清理过期付款单...");
        int count = payService.expirePendingOrders();
        log.info("过期付款单清理完成, 共清理{}条", count);
    }
}
