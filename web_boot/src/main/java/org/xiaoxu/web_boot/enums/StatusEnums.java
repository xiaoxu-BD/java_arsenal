package org.xiaoxu.web_boot.enums;

import lombok.Getter;

/**
 * @className: StatusEnums
 * @author: xiaoxu
 * @date: 2025/12/28 10:05
 * @Version: 1.0
 * @description:
 */
@Getter
public enum StatusEnums {


    INACTIVE(0,"禁用"),
    ACTIVE(1,"启用"),
    ;


    private Integer code;

    private String desc;


    StatusEnums(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
