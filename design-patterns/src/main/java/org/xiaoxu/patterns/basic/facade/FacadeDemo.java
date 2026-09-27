package org.xiaoxu.patterns.basic.facade;

/**
 * 外观模式演示（初级版）
 *
 * <p>对比：不用门面，客户端要自己 new 3 个设备并记住开关顺序；
 * 用了门面，客户端只认识 watchMovie / endMovie 两个词。
 */
public class FacadeDemo {

    public static void main(String[] args) {
        HomeTheaterFacade theater = new HomeTheaterFacade();
        theater.watchMovie();
        System.out.println("-------- 看完 --------");
        theater.endMovie();
    }
}
