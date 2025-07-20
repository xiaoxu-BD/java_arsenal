package org.xiaoxu.web_boot.entity;

import lombok.Data;

@Data
public class Address {
    private Long id;
    private Long personId;
    private String province;
    private String city;
    private String detail;
    private String type;
}