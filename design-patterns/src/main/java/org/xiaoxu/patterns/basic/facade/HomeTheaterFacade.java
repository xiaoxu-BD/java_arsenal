package org.xiaoxu.patterns.basic.facade;

/**
 * 家庭影院门面（Facade）：把"开 3 个设备"压缩成"看电影"一个动作
 *
 * <p>外观模式：客户端只需要"看电影的入口"，不关心背后有多少子系统、以什么顺序开关。
 * 它不改子系统的任何代码，只是收口——这正是"迪米特法则（最少知识原则）"的落地。
 *
 * <p>日常开发中的同款：一次"下单"门面方法内部编排库存、支付、物流多个服务
 * （见 advanced 包），以及日志门面 slf4j。
 */
public class HomeTheaterFacade {

    private final Projector projector = new Projector();
    private final Screen screen = new Screen();
    private final SoundSystem soundSystem = new SoundSystem();

    /** 一键观影：客户端只调这一个方法 */
    public void watchMovie() {
        System.out.println("【准备观影】");
        screen.down();
        projector.on();
        soundSystem.on();
    }

    /** 一键关闭 */
    public void endMovie() {
        System.out.println("【关闭影院】");
        soundSystem.off();
        projector.off();
        screen.up();
    }
}
