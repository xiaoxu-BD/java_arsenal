package org.xiaoxu.patterns.advanced.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 策略注册表工厂（高级版）
 *
 * <p>它是"简单工厂的 switch"与"工厂方法的类爆炸"之间的第三条路：
 * 用一个 {@code Map<code, strategy>} 做注册表，策略来"报名"，工厂按编码取用。
 *
 * <p>对比每天在业务代码里写 if-else 的三个痛点：
 * <ol>
 *   <li>新增渠道要改核心方法（违反开闭原则，回归测试范围越滚越大）；</li>
 *   <li>方法被各种渠道的分支撑到几百行；</li>
 *   <li>想单独测试某个渠道，得构造一大坨无关上下文。</li>
 * </ol>
 *
 * <p>Spring 项目里最常见的落地方式：注入 {@code List<DiscountStrategy>}
 * 遍历 {@code registry.register(strategy)}，与这里结构完全一致。
 */
public final class StrategyRegistry {

    /** 注册表：编码 -> 策略。启动期注册、运行期只读，HashMap 足够；若要运行期动态注册换 ConcurrentHashMap */
    private static final Map<String, DiscountStrategy> REGISTRY = new HashMap<>();

    static {
        // 内置策略统一在此报名；也可以改成 SPI / Spring 注入，进一步做到零登记
        register(new FullReduceStrategy());
        register(new VipDiscountStrategy());
        register(new NewUserStrategy());
    }

    private StrategyRegistry() {
    }

    /** 注册一个策略：策略重复注册直接抛错，避免静默覆盖线上配置 */
    public static void register(DiscountStrategy strategy) {
        DiscountStrategy old = REGISTRY.putIfAbsent(strategy.code(), strategy);
        if (old != null) {
            throw new IllegalStateException("策略编码重复注册: " + strategy.code());
        }
    }

    /** 按编码取策略：返回 Optional，把"编码配错了"这个业务异常交给调用方决定怎么处理 */
    public static Optional<DiscountStrategy> get(String code) {
        return Optional.ofNullable(REGISTRY.get(code));
    }

    /** 防御式便利方法：取不到直接抛业务异常 */
    public static DiscountStrategy require(String code) {
        return get(code).orElseThrow(() ->
                new IllegalArgumentException("未注册的促销策略: " + code));
    }
}
