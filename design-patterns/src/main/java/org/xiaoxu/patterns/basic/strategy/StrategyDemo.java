package org.xiaoxu.patterns.basic.strategy;

/**
 * 策略模式演示（初级版）
 *
 * <p>课堂演示点：
 * <ol>
 *   <li>加一种出行方式（开车），只需新增一个类，Traveler 一行不改——开闭原则；</li>
 *   <li>对比"继承出 PlaneTraveler / TrainTraveler..."的写法——组合策略远比继承灵活。</li>
 * </ol>
 */
public class StrategyDemo {

    /** 上下文：持有策略，把"怎么出行"委托出去 */
    static class Traveler {

        private TravelStrategy strategy;

        public void setStrategy(TravelStrategy strategy) {
            this.strategy = strategy;   // 运行时随时换策略
        }

        public void travel(int distanceKm) {
            System.out.println("选择[" + strategy.name() + "]出行 " + distanceKm
                    + " 公里，预计 " + strategy.costHours(distanceKm) + " 小时");
        }
    }

    public static void main(String[] args) {
        Traveler traveler = new Traveler();
        int distance = 1200;

        traveler.setStrategy(new PlaneStrategy());
        traveler.travel(distance);

        traveler.setStrategy(new TrainStrategy());   // 换策略不用改上下文
        traveler.travel(distance);

        traveler.setStrategy(new BicycleStrategy());
        traveler.travel(distance);
    }
}
