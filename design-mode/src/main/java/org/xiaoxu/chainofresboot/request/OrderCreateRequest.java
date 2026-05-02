package org.xiaoxu.chainofresboot.request;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateRequest {
    private Long userId;
    private Long goodsId;
    private Integer count;
}
