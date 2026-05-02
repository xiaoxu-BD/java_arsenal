package org.xiaoxu.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductInitRequest {
    private String name;
    private String memo;
    private BigDecimal price;
    private String priUrl;
    private Integer totalStock;
}
