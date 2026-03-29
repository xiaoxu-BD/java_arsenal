package org.xiaoxu.invoicep.enums;

import lombok.Getter;

@Getter
public enum InvoiceStatusEnum {
    NORMAL(1, "正常"),
    REVERSED(2, "已冲销");
    // 省略属性和构造函数



    private Integer code;
    private String desc;

    InvoiceStatusEnum(Integer code,String desc){
        this.code  = code;
        this.desc = desc;
    }
}