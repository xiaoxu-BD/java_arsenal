package org.xiaoxu.patterns.basic.strategy;

/** 具体策略：自行车 */
public class BicycleStrategy implements TravelStrategy {

    @Override
    public int costHours(int distanceKm) {
        return distanceKm / 15;
    }

    @Override
    public String name() {
        return "自行车";
    }
}
