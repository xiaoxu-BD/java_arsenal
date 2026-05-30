package org.xiaoxu.se.serializable;

import java.io.Serializable;

/**
 * 序列化实体类
 * 必须实现 Serializable 接口（标记接口，没有任何方法）
 */
public class Student implements Serializable {

    // 序列化版本号 — 反序列化时会校验，不一致则抛 InvalidClassException
    private static final long serialVersionUID = 1L;

    private String name;
    private int age;

    // transient：该字段不会被序列化，反序列化后为默认值（null / 0 / false）
    private transient String password;

    // static：属于类，不参与实例的序列化
    private static String school = "清华大学";

    public Student(String name, int age, String password) {
        this.name = name;
        this.age = age;
        this.password = password;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getPassword() { return password; }

    @Override
    public String toString() {
        return "Student{name='" + name + "', age=" + age
                + ", password='" + password + "'"
                + ", school='" + school + "'}";
    }

    public static String getSchool() { return school; }
    public static void setSchool(String s) { school = s; }
}
