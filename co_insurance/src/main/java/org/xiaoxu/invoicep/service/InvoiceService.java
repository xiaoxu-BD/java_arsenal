package org.xiaoxu.invoicep.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaoxu.invoicep.enetity.Invoice;
import org.xiaoxu.invoicep.enums.InvoiceStatusEnum;
import org.xiaoxu.invoicep.enums.InvoiceTypeEnum;
import org.xiaoxu.invoicep.mapper.InvoiceMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class InvoiceService extends ServiceImpl<InvoiceMapper, Invoice> {

    /**
     * 开具正数蓝字发票 (模拟正常开票)
     */
    @Transactional(rollbackFor = Exception.class)
    public Invoice issueBlueInvoice(String orderId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("蓝字发票金额必须大于0");
        }
        
        Invoice blueInvoice = new Invoice();
        blueInvoice.setOrderId(orderId);
        blueInvoice.setAmount(amount);
        blueInvoice.setType(InvoiceTypeEnum.BLUE.getCode());
        blueInvoice.setStatus(InvoiceStatusEnum.NORMAL.getCode());
        blueInvoice.setCreateTime(LocalDateTime.now());
        
        this.save(blueInvoice);
        return blueInvoice;
    }

    /**
     * 执行红蓝冲销逻辑
     * @param originalInvoiceId 需要冲销的蓝字发票ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Invoice reverseInvoice(Long originalInvoiceId) {
        // 1. 查询并校验原发票
        Invoice blueInvoice = this.getById(originalInvoiceId);
        if (blueInvoice == null) {
            throw new RuntimeException("原发票不存在");
        }
        
        // 校验：只有蓝票可以被冲销
        if (!InvoiceTypeEnum.BLUE.getCode().equals(blueInvoice.getType())) {
            throw new RuntimeException("只能对蓝字发票进行冲销");
        }
        
        // 校验：防止重复冲销（状态机校验）
        if (InvoiceStatusEnum.REVERSED.getCode().equals(blueInvoice.getStatus())) {
            throw new RuntimeException("该发票已被冲销，请勿重复操作");
        }

        // 2. 构造红字发票 (金额取反)
        Invoice redInvoice = new Invoice();
        redInvoice.setOrderId(blueInvoice.getOrderId());
        redInvoice.setAmount(blueInvoice.getAmount().negate()); // 核心：正数变负数
        redInvoice.setType(InvoiceTypeEnum.RED.getCode());
        redInvoice.setStatus(InvoiceStatusEnum.NORMAL.getCode()); 
        redInvoice.setOriginalInvoiceId(blueInvoice.getId()); // 绑定关联关系
        redInvoice.setCreateTime(LocalDateTime.now());

        // 3. 更新原蓝字发票状态为“已冲销”
        blueInvoice.setStatus(InvoiceStatusEnum.REVERSED.getCode());
        this.updateById(blueInvoice);

        // 4. 保存红字发票
        this.save(redInvoice);
        
        return redInvoice;
    }
}