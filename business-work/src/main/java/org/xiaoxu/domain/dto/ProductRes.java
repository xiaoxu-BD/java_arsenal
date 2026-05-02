package org.xiaoxu.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRes {
    private String name;
    private String memo;
    private BigDecimal price;
    private Long id;
}
