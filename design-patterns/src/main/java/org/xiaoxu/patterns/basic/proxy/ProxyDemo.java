package org.xiaoxu.patterns.basic.proxy;

/**
 * 静态代理演示（初级版）
 *
 * <p>活动方只拿到 Star 接口，全程跟"经纪人"打交道，却听到的正是明星的歌声。
 */
public class ProxyDemo {

    public static void main(String[] args) {
        Star agent = new StarAgent(new RealStar());

        agent.negotiate();   // 经纪人挡掉
        System.out.println("----");
        agent.sing();        // 经纪人放行 -> 明星唱 -> 经纪人收尾
    }
}
