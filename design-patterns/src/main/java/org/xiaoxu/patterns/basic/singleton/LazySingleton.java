package org.xiaoxu.patterns.basic.singleton;

/**
 * 懒汉式单例 —— 双重检锁 DCL（初级版）
 *
 * <p>"懒"：第一次真正使用时才创建实例，避免像饿汉式那样白占内存。
 *
 * <p>为什么要这么写（课堂演示点，逐行讲）：
 * <ol>
 *   <li>只判空一次行不行？——不行，多个线程可能同时通过第一次判空，各 new 一个；</li>
 *   <li>整个方法加 synchronized 行不行？——行，但每次调用都抢锁，性能差；</li>
 *   <li>于是"双重检锁"：第一次判空挡住绝大多数调用（无锁快路径），
 *       只有第一次创建实例时才进同步块，进去了再判一次空，防止等锁的线程重复创建；</li>
 *   <li>instance 必须加 volatile：防止"new 对象"被指令重排。
 *       new 其实是三步（分配内存 -> 初始化 -> 指向地址），重排后别的线程可能拿到"半成品"对象。</li>
 * </ol>
 */
public class LazySingleton {

    /** volatile：禁止指令重排，保证别的线程要么看到 null，要么看到完整对象 */
    private static volatile LazySingleton instance;

    private LazySingleton() {
        System.out.println("LazySingleton 实例被创建（第一次调用 getInstance 时才执行）");
    }

    public static LazySingleton getInstance() {
        if (instance == null) {                 // 第一次判空：实例已存在时直接返回，不用抢锁
            synchronized (LazySingleton.class) {
                if (instance == null) {         // 第二次判空：等锁的线程进来后可能已被别人创建
                    instance = new LazySingleton();
                }
            }
        }
        return instance;
    }
}
