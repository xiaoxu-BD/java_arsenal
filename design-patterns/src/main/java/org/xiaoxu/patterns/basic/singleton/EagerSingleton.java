package org.xiaoxu.patterns.basic.singleton;

/**
 * 饿汉式单例（初级版）
 *
 * <p>类加载时就创建好实例（"饿"：急着吃），JVM 类加载机制保证线程安全。
 *
 * <p>优点：写法最简单、无线程安全问题。
 * <p>缺点：不管用不用，类加载时就占一块内存；如果构造里有重活，还会拖慢类加载。
 *
 * <p>课堂演示点：不管调用多少次 getInstance()，拿到的都是同一个对象。
 */
public class EagerSingleton {

    /** static final：类加载阶段就初始化，且不可再被赋值 */
    private static final EagerSingleton INSTANCE = new EagerSingleton();

    /** 私有构造：外界不能 new，只能走 getInstance() 拿唯一入口 */
    private EagerSingleton() {
        System.out.println("EagerSingleton 实例被创建（类加载时执行一次）");
    }

    public static EagerSingleton getInstance() {
        return INSTANCE;
    }
}
