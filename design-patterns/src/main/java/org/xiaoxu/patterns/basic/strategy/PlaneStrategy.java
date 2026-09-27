package org.xiaoxu.patterns.basic.strategy;

/** 具体策略：飞机 */
public class PlaneStrategy implements TravelStrategy {

    @Override
    public int costHours(int distanceKm) {
        return Math.max(1, distanceKm / 800);
    }

    @Override
    public String name() {
        return "飞机";
    }
}
