package org.xiaoxu.web_boot.entity.vo;

import lombok.Data;

/**
 * @className: AddressVO
 * @author: xiaoxu
 * @date: 2025/9/29 11:18
 * @Version: 1.0
 * @description:
 */
@Data
public class AddressVO {
    private Long id;
    private Long personId;
    private String province;
    private String city;
}
