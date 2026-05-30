package org.xiaoxu.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
public class ModTab {
    private Long id;

    private String userName;

    private Integer userAge;

    private String userEmail;

    private String phoneNumber;

    private String idCardNo;

    private BigDecimal orderAmount;

    private BigDecimal accountBalance;

    private Date createTime;

    private Date updateTime;

    private Byte deleteFlag;

    private Integer versionNo;

    private String companyCode;

    private String productType;

    private String lastLoginIp;

    private Integer loginCount;

    private String remarkText;

}