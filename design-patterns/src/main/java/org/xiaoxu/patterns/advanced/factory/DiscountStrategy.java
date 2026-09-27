package org.xiaoxu.patterns.advanced.factory;

import java.math.BigDecimal;

/**
 * 促销策略接口（高级版）
 *
 * <p>与 basic 版最大的不同：接口自带 {@link #code()}——每个策略自己"报名"注册键。
 * 工厂据此建注册表，之后<b>新增策略不再需要修改任何旧代码</b>（开闭原则的完全体）。
 */
public interface DiscountStrategy {

    /** 策略编码：对外配置/前端传参使用的标识，如 "FULL_REDUCE" */
    String code();

    /** 计算促销价（金额计算一律 BigDecimal，杜绝 double 精度问题） */
    BigDecimal apply(BigDecimal price);
}
