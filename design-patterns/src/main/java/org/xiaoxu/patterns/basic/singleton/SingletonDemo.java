package org.xiaoxu.patterns.basic.singleton;

import java.util.concurrent.CountDownLatch;

/**
 * 单例模式演示（初级版）
 *
 * <p>运行 main 可以看到：
 * <ol>
 *   <li>构造方法只打印一次——实例只创建了一次；</li>
 *   <li>单线程、10 个线程并发拿到的实例 hashCode 全部相同。</li>
 * </ol>
 */
public class SingletonDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========== 1. 饿汉式 ==========");
        EagerSingleton a = EagerSingleton.getInstance();
        EagerSingleton b = EagerSingleton.getInstance();
        System.out.println("两次获取是否同一个对象: " + (a == b));

        System.out.println("\n========== 2. 懒汉式（双重检锁） ==========");
        LazySingleton c = LazySingleton.getInstance();
        LazySingleton d = LazySingleton.getInstance();
        System.out.println("两次获取是否同一个对象: " + (c == d));

        System.out.println("\n========== 3. 多线程并发验证懒汉式 ==========");
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                // hashCode 相同 => 十个线程拿到的是同一个 LazySingleton
                System.out.println(Thread.currentThread().getName()
                        + " -> " + System.identityHashCode(LazySingleton.getInstance()));
                latch.countDown();
            }).start();
        }
        latch.await();
        System.out.println("所有线程拿到的 hashCode 一致，单例成立");
    }
}
