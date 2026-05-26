package org.xiaoxu.deepcopy;

import java.io.*;

/**
 * 深浅拷贝演示
 *
 * 浅拷贝：复制对象本身，但内部的引用字段仍指向同一个对象
 * 深拷贝：复制对象本身，内部的引用字段也指向全新的副本对象
 */
public class DeepCopy implements Cloneable, Serializable {

    private String name;
    private Address address;  // 引用类型字段 — 这是区分深浅拷贝的关键

    public DeepCopy(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    // ========== 浅拷贝：只重写 clone()，不拷贝引用字段 ==========
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // ========== 深拷贝方式一：手动逐层 clone ==========
    protected DeepCopy deepCloneManual() throws CloneNotSupportedException {
        DeepCopy copy = (DeepCopy) super.clone();
        copy.address = (Address) this.address.clone();  // 引用字段也 clone
        return copy;
    }

    // ========== 深拷贝方式二：序列化 ==========
    protected DeepCopy deepCloneBySerialize() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (DeepCopy) ois.readObject();
    }

    public static void main(String[] args) throws Exception {

        // 原始对象
        DeepCopy original = new DeepCopy("张三", new Address("北京", "朝阳区"));
        System.out.println("===== 原始对象 =====");
        System.out.println("original    : " + original);
        System.out.println("address hash: " + System.identityHashCode(original.getAddress()));

        // ---------- 浅拷贝 ----------
        DeepCopy shallow = (DeepCopy) original.clone();
        System.out.println("\n===== 浅拷贝 =====");
        System.out.println("shallow     : " + shallow);
        System.out.println("address hash: " + System.identityHashCode(shallow.getAddress()));
        System.out.println("address 是同一对象？ " + (original.getAddress() == shallow.getAddress()));

        // 修改拷贝对象的引用字段
        shallow.getAddress().setCity("上海");
        System.out.println("\n修改 shallow.address.city → 上海");
        System.out.println("original.address.city = " + original.getAddress().getCity());  // 也被改了！
        System.out.println("shallow.address.city  = " + shallow.getAddress().getCity());

        // ---------- 深拷贝（手动 clone） ----------
        DeepCopy deep = original.deepCloneManual();
        System.out.println("\n===== 深拷贝（手动 clone） =====");
        System.out.println("deep        : " + deep);
        System.out.println("address hash: " + System.identityHashCode(deep.getAddress()));
        System.out.println("address 是同一对象？ " + (original.getAddress() == deep.getAddress()));

        deep.getAddress().setCity("广州");
        System.out.println("\n修改 deep.address.city → 广州");
        System.out.println("original.address.city = " + original.getAddress().getCity());  // 不受影响
        System.out.println("deep.address.city     = " + deep.getAddress().getCity());

        // ---------- 深拷贝（序列化） ----------
        DeepCopy deepSer = original.deepCloneBySerialize();
        System.out.println("\n===== 深拷贝（序列化） =====");
        System.out.println("deepSer     : " + deepSer);
        System.out.println("address hash: " + System.identityHashCode(deepSer.getAddress()));
        System.out.println("address 是同一对象？ " + (original.getAddress() == deepSer.getAddress()));

        // ---------- 总结 ----------
        System.out.println("\n===== 结论 =====");
        System.out.println("浅拷贝：对象复制了，但内部 address 仍指向同一块内存 → 改一个两个都变");
        System.out.println("深拷贝：对象和内部 address 都是全新的副本 → 改一个另一个不受影响");
        System.out.println("clone() 默认是浅拷贝，深拷贝需要手动逐层 clone 或用序列化");
    }

    @Override
    public String toString() {
        return "name=" + name + ", address=" + address;
    }

    public Address getAddress() {
        return address;
    }
}

// 引用类型的字段，也实现 Cloneable
class Address implements Cloneable, Serializable {
    private String city;
    private String district;

    public Address(String city, String district) {
        this.city = city;
        this.district = district;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    @Override
    public String toString() {
        return city + " " + district;
    }
}
