package org.xiaoxu.patterns.basic.prototype;

/**
 * 原型模式演示（初级版）
 *
 * <p>课堂演示点：
 * <ol>
 *   <li>浅拷贝：改克隆羊的基本类型不影响原件，但改"朋友"的名字，原件跟着变——共享同一个 Friend；</li>
 *   <li>深拷贝：两羊各有各的朋友，互不影响。</li>
 * </ol>
 */
public class PrototypeDemo {

    public static void main(String[] args) {
        Sheep dolly = new Sheep("多莉", 2, new Sheep.Friend("喜羊羊"));

        System.out.println("========== 1. 浅拷贝 ==========");
        Sheep shallow = dolly.clone();
        shallow.setName("克隆羊");
        shallow.getFriend().setName("懒羊羊");   // 改的是"共享的朋友"

        System.out.println("原件: " + dolly.getName() + "，朋友=" + dolly.getFriend().getName());
        System.out.println("克隆: " + shallow.getName() + "，朋友=" + shallow.getFriend().getName());
        System.out.println("两只羊的朋友是同一个对象吗: " + (dolly.getFriend() == shallow.getFriend()));

        System.out.println("\n========== 2. 深拷贝 ==========");
        Sheep.Friend friend = new Sheep.Friend("沸羊羊");
        Sheep original = new Sheep("美羊羊", 3, friend);
        Sheep deep = original.deepClone();
        deep.getFriend().setName("暖羊羊");       // 只改克隆体的朋友

        System.out.println("原件朋友=" + original.getFriend().getName());
        System.out.println("深拷贝朋友=" + deep.getFriend().getName());
        System.out.println("两只羊的朋友是同一个对象吗: " + (original.getFriend() == deep.getFriend()));
    }
}
