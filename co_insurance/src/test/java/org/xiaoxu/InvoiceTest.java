package org.xiaoxu;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.xiaoxu.invoicep.enetity.Invoice;
import org.xiaoxu.invoicep.mapper.InvoiceMapper;
import org.xiaoxu.invoicep.service.InvoiceService;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * @className: InvoiceTest
 * @author: xiaoxu
 * @date: 2026/3/28 16:13
 * @Version: 1.0
 * @description:
 */
@Slf4j
@SpringBootTest
public class InvoiceTest {


    @Autowired
    InvoiceMapper invoiceMapper;


    @Autowired
    InvoiceService invoiceService;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;





    @Test
    public void testIssueInvoice(){

        String orderId = UUID.randomUUID().toString().replace("_","");
        BigDecimal amount = new BigDecimal("299");
        Invoice invoice = invoiceService.issueBlueInvoice(orderId, amount);

        log.info("invoice:{}",invoice);
    }

    @Test
    public void testReverseInvoice(){
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<Invoice>().eq(Invoice::getOrderId, "080f998e-13f2-4071-8f60-993b66ca8eeb").eq(Invoice::getAmount, BigDecimal.valueOf(299)).eq(Invoice::getType, 1);
        Invoice buleInvoice = invoiceMapper.selectOne(wrapper);
        if (!Objects.isNull(buleInvoice)){
            redisTemplate.opsForValue().set("invoice:"+buleInvoice.getId(),buleInvoice);
        }
//        Invoice invoice = invoiceService.reverseInvoice(buleInvoice.getId());
        log.info("redInvoice:{}",buleInvoice);
    }


}
