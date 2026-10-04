package org.xiaoxu.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @className: PracticeProduct
 * @description: 练习-商品表(practice_product),配合 XML mapper 教学
 */
@Data
public class PracticeProduct {
    private Long id;
    private String productName;
    /** 类目:DIGITAL数码/CLOTHES服饰/FOOD食品 */
    private String category;
    private BigDecimal price;
    private Integer stock;
    /** 状态:0下架 1上架 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
