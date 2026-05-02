package org.xiaoxu.domain.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeckillRequest {

    private Long productId;
    private Long userId;
    private Integer quantity;
}
