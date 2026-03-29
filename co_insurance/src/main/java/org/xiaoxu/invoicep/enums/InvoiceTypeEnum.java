package org.xiaoxu.invoicep.enums;

import lombok.Getter;

@Getter
public enum InvoiceTypeEnum {
    BLUE(1, "蓝字发票"),
    RED(2, "红字发票");


    // 省略属性和构造函数



    private Integer code;
    private String desc;


     InvoiceTypeEnum(Integer code,String desc){
        this.code = code;
        this.desc = desc;
    }
}

