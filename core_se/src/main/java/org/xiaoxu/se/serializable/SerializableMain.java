package org.xiaoxu.se.serializable;

import com.alibaba.fastjson2.JSON;

import java.io.*;

/**
 * Java 序列化机制演示
 *
 * 序列化：对象 → 字节流（写入文件/网络传输）
 * 反序列化：字节流 → 对象（从文件/网络还原）
 */
public class SerializableMain {

    private static final String FILE_PATH = "student.ser";

    public static void main(String[] args) throws Exception {

        // 1. 基本序列化与反序列化
//        basicDemo();

        // 2. transient 效果
//        transientDemo();

        // 3. serialVersionUID 作用
        // serialVersionUiddDemo();  // 需要修改 Student 类后对比测试，这里先注释

        // 4. JSON 序列化
        jsonDemo();
    }

    /**
     * 1. 基本序列化 & 反序列化
     */
    private static void basicDemo() throws Exception {
        System.out.println("===== 1. 基本序列化 =====");

        Student stu = new Student("张三", 20, "123456");
        System.out.println("序列化前: " + stu);

        // 序列化：Object → 文件
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(stu);
        }
        System.out.println("已写入文件: " + FILE_PATH);

        // 反序列化：文件 → Object
        Student restored;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            restored = (Student) ois.readObject();
        }
        System.out.println("反序列化后: " + restored);
        System.out.println("是同一个对象？ " + (stu == restored));  // false，全新的对象

        // 清理文件
//        new File(FILE_PATH).delete();
    }

    /**
     * 2. transient 关键字：标记不参与序列化的字段
     */
    private static void transientDemo() throws Exception {
        System.out.println("\n===== 2. transient 演示 =====");

        Student stu = new Student("李四", 22, "secret123");
        System.out.println("序列化前: " + stu);

        // 序列化 → 反序列化
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH));
        oos.writeObject(stu);
        oos.close();

        // 修改 static 字段（模拟另一个 JVM / 另一次运行）
        Student.setSchool("北京大学");

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH));
        Student restored = (Student) ois.readObject();
        ois.close();

        System.out.println("反序列化后: " + restored);
        System.out.println("  name 恢复: " + restored);
        System.out.println("  password(transient) = null → " + (restored.toString().contains("password='null'")));
        System.out.println("  school(static) = 北京大学 → 跟随当前类的静态值，不受序列化影响");

        // 还原
        Student.setSchool("清华大学");
        new File(FILE_PATH).delete();
    }

    /**
     * 3. serialVersionUID
     *
     * 序列化时会把 serialVersionUID 写入字节流
     * 反序列化时会比对：类的 serialVersionUID vs 流中的 serialVersionUID
     * 不一致 → 抛 InvalidClassException
     *
     * 不显式声明时，编译器会根据类结构自动生成
     * 所以加字段/改方法会导致自动生成的值变化 → 旧的 .ser 文件就反序列化失败了
     *
     * 建议：始终显式声明 serialVersionUID = 1L;
     */

    // ========== JSON 序列化（fastjson2） ==========

    private static void jsonDemo() throws Exception {
        System.out.println("\n===== 4. JSON 序列化（对比 Java 序列化） =====");

        Student stu = new Student("王五", 25, "mypassword");

        // 对象 → JSON 字符串
        String json = JSON.toJSONString(stu);
        System.out.println("JSON 序列化: " + json);
        // 注意：transient 对 JSON 无效，password 照样输出！
        // Java 序列化的 transient 是 JVM 级别的，JSON 库不认这个关键字

        // JSON 字符串 → 对象
        Student fromJson = JSON.parseObject(json, Student.class);
        System.out.println("JSON 反序列化: " + fromJson);

        // 写入 JSON 文件
        String jsonFile = "student.json";
        try (FileWriter fw = new FileWriter(jsonFile)) {
            fw.write(json);
        }
        System.out.println("已写入文件: " + jsonFile);

        // 读取 JSON 文件 → 对象
        String content;
        try (BufferedReader br = new BufferedReader(new FileReader(jsonFile))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            content = sb.toString();
        }
        Student fromFile = JSON.parseObject(content, Student.class);
        System.out.println("从文件读取: " + fromFile);

//        new File(jsonFile).delete();
    }
}
