package org.xiaoxu.invoicep.enetity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("invoice")
@Data
public class Invoice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderId;
    private BigDecimal amount;
    private Integer type;
    private Integer status;
    private Long originalInvoiceId;
    private LocalDateTime createTime;
    
    // 省略 Getter / Setter
}