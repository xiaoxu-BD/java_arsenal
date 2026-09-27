package org.xiaoxu.patterns.basic.proxy;

/** 真实对象（Real Subject）：明星本人，只干专业的事 */
public class RealStar implements Star {

    @Override
    public void negotiate() {
        System.out.println("明星本人：谈价？让我的经纪人跟你谈");
    }

    @Override
    public void sing() {
        System.out.println("明星本人：登台唱歌");
    }
}
