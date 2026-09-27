package org.xiaoxu.patterns.advanced.prototype;

import java.io.*;

/**
 * 序列化深拷贝工具（高级版原型模式）
 *
 * <p>basic 版深拷贝靠手写 clone 逐字段复制，字段一多就漏、就难维护。
 * 这里换成"序列化 → 反序列化"整棵对象图一次复制：
 * <ul>
 *   <li>优点：任意深度的嵌套结构通吃，代码只有一份；</li>
 *   <li>限制：对象图里所有类必须实现 {@link Serializable}，transient 字段会丢失，
 *       性能比手写拷贝差——高频路径慎用；</li>
 *   <li>日常替代品：JSON 序列化（fastjson/Jackson）round-trip，
 *       不要求 Serializable，原理一模一样。</li>
 * </ul>
 */
public final class DeepCopyUtil {

    private DeepCopyUtil() {
    }

    /**
     * 深拷贝任意可序列化对象
     *
     * @param source 原对象（允许为 null）
     * @return 与原对象字段全等、但对象图完全独立的新对象
     * @throws UncheckedIOException 序列化失败（多半是某个字段没实现 Serializable）
     */
    public static <T extends Serializable> T deepCopy(T source) {
        if (source == null) {
            return null;
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(source);
            try (ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()))) {
                @SuppressWarnings("unchecked")
                T copy = (T) ois.readObject();
                return copy;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("深拷贝失败: " + source.getClass().getName(), e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("深拷贝失败: 反序列化找不到类", e);
        }
    }
}
