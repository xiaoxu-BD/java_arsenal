package org.xiaoxu.patterns.basic.strategy;

/**
 * 出行策略（Strategy）：一族可互换的算法
 *
 * <p>定义一族"做同一件事的方式"，让它们可以互相替换，调用方运行时随便换。
 * 三个前提都满足就该用策略：行为有多种 / 行为随场景变化 / 行为未来可能新增。
 */
public interface TravelStrategy {

    /** 计算本次出行耗时（小时） */
    int costHours(int distanceKm);

    /** 出行方式名 */
    String name();
}
