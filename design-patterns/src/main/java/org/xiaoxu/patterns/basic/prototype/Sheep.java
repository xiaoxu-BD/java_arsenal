package org.xiaoxu.patterns.basic.prototype;

/**
 * 克隆羊多莉（初级版）
 *
 * <p>原型模式：不重新 new，而是拿现有对象当"模板"复制一份。
 * 适用场景：对象创建成本高（字段多/查过库），且新对象和模板大体相同。
 *
 * <p>Java 的克隆基于 {@link Cloneable} + {@link Object#clone()}（浅拷贝）。
 * <b>浅拷贝是本案例的核心考点</b>：引用类型字段只复制"地址"，两只羊共享同一个朋友。
 */
public class Sheep implements Cloneable {

    private String name;
    private int age;
    /** 引用类型字段：浅拷贝时两只羊共享它，深拷贝时各自独立 */
    private Friend friend;

    public Sheep(String name, int age, Friend friend) {
        this.name = name;
        this.age = age;
        this.friend = friend;
    }

    /**
     * 浅拷贝：Object.clone() 逐位复制——基本类型复制值，引用类型只复制地址。
     * 注意访问权限是 protected，这里放开成 public 方便外部调用。
     */
    @Override
    public Sheep clone() {
        try {
            return (Sheep) super.clone();
        } catch (CloneNotSupportedException e) {
            // 实现了 Cloneable 就不会走到这里，兜底转成运行时异常
            throw new AssertionError(e);
        }
    }

    /**
     * 深拷贝：在浅拷贝的基础上，把引用类型字段也再造一份，两羊彻底独立。
     * 字段一多这个方法就难写难维护——工程上更推荐序列化方式（见 advanced 包）。
     */
    public Sheep deepClone() {
        Sheep copy = clone();
        copy.friend = new Friend(this.friend.getName());
        return copy;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public Friend getFriend() { return friend; }

    /** 朋友（引用类型字段，用于演示浅/深拷贝差异） */
    public static class Friend {
        private String name;

        public Friend(String name) { this.name = name; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
