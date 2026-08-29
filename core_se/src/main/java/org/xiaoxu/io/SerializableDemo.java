package org.xiaoxu.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * L3 练习: 对象序列化。对象 --writeObject--> 字节 --readObject--> 对象。
 * 网络传对象的祖师爷(老 RMI/Socket 时代), 现在生产被 JSON/Protobuf 取代,
 * 但"对象与字节互转"的概念是所有序列化框架的母题, 值得亲手过一遍。
 *
 * 验证点:
 *   1. transient 的 password 反序列化后是 null
 *   2. 深拷贝副本改了内部 List, 原件不受影响
 *   3. 把 serialVersionUID 删掉/改值, 再用旧文件反序列化 -> InvalidClassException
 */
public class SerializableDemo {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Path dir = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "io-practice"));

        serializeToFile(dir.resolve("03-用户对象.bin"));
        deepCopyByMemory();
    }

    /** 演示1: 对象 -> 文件 -> 读回 */
    static void serializeToFile(Path file) throws IOException, ClassNotFoundException {
        User user = new User("张三", 25, "密码不该被序列化", new ArrayList<>(List.of("研发部", "A栋")));

        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(file.toFile()))) {
            out.writeObject(user);
        }

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(file.toFile()))) {
            User back = (User) in.readObject();
            System.out.println("✅ 从文件读回: " + back);
            System.out.println("   transient 密码 = " + back.password + " (没被序列化, 是 null)");
        }
    }

    /**
     * 演示2: 内存流深拷贝。不落盘, 对象写进内存管子再读回来, 得到一棵完全独立的对象树。
     * 面试高频题; 也顺便证明"流不等于文件, 管子的另一头可以是内存"。
     */
    static User deepCopy(User origin) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream mem = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(mem)) {
            out.writeObject(origin);
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(mem.toByteArray()))) {
            return (User) in.readObject();
        }
    }

    static void deepCopyByMemory() throws IOException, ClassNotFoundException {
        User origin = new User("李四", 30, "pw", new ArrayList<>(List.of("市场部")));
        User copy = deepCopy(origin);
        copy.getTags().add("我是副本加的, 原件不该有");

        System.out.println("✅ 深拷贝验证: 原件 tags=" + origin.getTags() + ", 副本 tags=" + copy.getTags());
    }

    /**
     * serialVersionUID: 类的"版本号"。反序列化时 JVM 拿它和字节流里记录的比对,
     * 对不上就抛 InvalidClassException 拒绝还原 —— 防止拿旧数据还原出新类的错乱状态。
     * 显式声明它, 加字段还能兼容旧文件; 不声明则编译器按类结构自动算, 改一个字段就全旧文件报废。
     */
    public static class User implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private int age;
        /** transient = 这次搬运不带上我。密码/临时状态字段的标准用法 */
        private transient String password;
        private List<String> tags;

        public User(String name, int age, String password, List<String> tags) {
            this.name = name;
            this.age = age;
            this.password = password;
            this.tags = tags;
        }

        public List<String> getTags() {
            return tags;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + ", tags=" + tags + "}";
        }
    }
}
