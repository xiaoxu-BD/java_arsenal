package org.xiaoxu.invoicep.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.invoicep.enetity.Invoice;

@Mapper
public interface InvoiceMapper extends BaseMapper<Invoice> {
}
