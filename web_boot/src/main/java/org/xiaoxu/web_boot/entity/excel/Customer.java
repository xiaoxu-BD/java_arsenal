package org.xiaoxu.web_boot.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @className: Customer
 * @author: xiaoxu
 * @date: 2025/9/10 9:48
 * @Version: 1.0
 * @description:
 */
@Data
public class Customer {

    @ExcelProperty("ID")
    private String id;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("电话")
    private String phone;

    @ExcelProperty("邮件")
    private String email;
}
