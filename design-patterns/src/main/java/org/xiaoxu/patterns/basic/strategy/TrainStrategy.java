package org.xiaoxu.patterns.basic.strategy;

/** 具体策略：高铁 */
public class TrainStrategy implements TravelStrategy {

    @Override
    public int costHours(int distanceKm) {
        return Math.max(1, distanceKm / 300);
    }

    @Override
    public String name() {
        return "高铁";
    }
}
