package org.xiaoxu.patterns.advanced.singleton;

import org.xiaoxu.patterns.basic.singleton.LazySingleton;

import java.io.*;
import java.lang.reflect.Constructor;

/**
 * 单例的"攻防战"演示（高级版）
 *
 * <p>私有构造器挡得住 new，挡不住两样武器：
 * <ol>
 *   <li><b>反射</b>：setAccessible(true) 强行调用私有构造器；</li>
 *   <li><b>序列化</b>：writeObject/readObject 走的是"重建对象"，绕过 getInstance()。</li>
 * </ol>
 *
 * <p>运行本类可以看到三档防御强度：
 * basic 的 LazySingleton 被击穿 -> ConfigHolder 构造器二次校验挡住反射
 * -> 枚举单例两种武器全部免疫（日常开发的满分答案）。
 */
public class SingletonAttackDemo {

    public static void main(String[] args) throws Exception {
        reflectionAttackLazy();
        reflectionAttackHolder();
        reflectionAttackEnum();
        serializationAttack();
    }

    /** 1. 反射击穿普通懒汉式：两个实例诞生，单例宣告失败 */
    private static void reflectionAttackLazy() throws Exception {
        System.out.println("========== 1. 反射攻击 basic.LazySingleton ==========");
        LazySingleton normal = LazySingleton.getInstance();

        Constructor<LazySingleton> ctor = LazySingleton.class.getDeclaredConstructor();
        ctor.setAccessible(true);                          // 绕过 private 检查
        LazySingleton hacked = ctor.newInstance();

        System.out.println("正常实例: " + System.identityHashCode(normal));
        System.out.println("反射实例: " + System.identityHashCode(hacked));
        System.out.println("单例被破坏? " + (normal != hacked));
    }

    /** 2. 构造器二次校验挡住反射（攻击时机在首次初始化之后才有效） */
    private static void reflectionAttackHolder() {
        System.out.println("\n========== 2. 反射攻击 ConfigHolder（构造器防御） ==========");
        ConfigHolder.getInstance();                        // 先让单例就位

        try {
            Constructor<ConfigHolder> ctor = ConfigHolder.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();                            // 触发构造器里的二次校验
            System.out.println("攻击成功（不应该发生）");
        } catch (Exception e) {
            // 反射包的异常都包在 InvocationTargetException 里，真正抛出的是我们的 IllegalStateException
            System.out.println("攻击被拦截: " + e.getCause().getMessage());
        }
    }

    /** 3. 反射攻击枚举：JDK 层面直接拒绝 */
    private static void reflectionAttackEnum() {
        System.out.println("\n========== 3. 反射攻击枚举单例 ==========");
        try {
            Constructor<?> ctor = SequenceGenerator.class.getDeclaredConstructor(String.class, int.class);
            ctor.setAccessible(true);
            ctor.newInstance("HACKED", 99);
        } catch (Exception e) {
            // 枚举的拒绝异常由 JDK 直接抛出（没有 cause 包装），反射调用失败的才包在 InvocationTargetException 里
            Throwable reason = e.getCause() != null ? e.getCause() : e;
            System.out.println("攻击被拦截: " + reason.getMessage());
        }
        System.out.println("枚举实例仍只有一个: " + (SequenceGenerator.INSTANCE == SequenceGenerator.INSTANCE));
    }

    /** 4. 序列化攻击：readObject 会 new 新对象，除非类提供 readResolve 防御 */
    private static void serializationAttack() throws IOException, ClassNotFoundException {
        System.out.println("\n========== 4. 序列化攻击 ==========");

        // 反面教材：没写 readResolve 的"可序列化单例"
        Object broken = roundTrip(BrokenSingleton.getInstance());
        System.out.println("[无防御] 反序列化前: " + System.identityHashCode(BrokenSingleton.getInstance())
                + " / 后: " + System.identityHashCode(broken)
                + " -> 单例被破坏? " + (broken != BrokenSingleton.getInstance()));

        // 枚举：readObject 特殊处理枚举，直接返回已有常量
        Object same = roundTrip(SequenceGenerator.INSTANCE);
        System.out.println("[枚举] 反序列化前后是同一个对象? " + (same == SequenceGenerator.INSTANCE));
    }

    private static Object roundTrip(Object obj) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return ois.readObject();
        }
    }

    /** 反面教材：实现了 Serializable 却没写 readResolve */
    static class BrokenSingleton implements Serializable {
        private static final BrokenSingleton INSTANCE = new BrokenSingleton();

        private BrokenSingleton() { }

        static BrokenSingleton getInstance() { return INSTANCE; }
    }
}
