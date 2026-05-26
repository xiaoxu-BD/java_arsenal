package org.xiaoxu.se.foundation;

public class CommonAndFamiliarString {
    public static void main(String[] args) {

/*        // 1. String：不可变，每次修改都创建新对象
        stringDemo();*/

//        // 2. StringBuilder：可变，单线程用，性能最好
//       stringBuilderDemo();

        // 3. StringBuffer：可变，线程安全，多线程用
//        stringBufferDemo();
//
        // 4. 性能对比：循环拼接 10万次
        performanceCompare();
    }

    /**
     * String：不可变字符串
     * 每次 "修改" 其实是创建新对象，原对象不变
     */
    private static void stringDemo() {
        System.out.println("===== 1. String（不可变） =====");

        String s = "Hello";
        System.out.println("原始: " + s + "  hash=" + System.identityHashCode(s));

        String s2 = s + " World";
        System.out.println("拼接后: " + s2 + "  hash=" + System.identityHashCode(s2));

        // s 本身没变
        System.out.println("原 s 仍是: " + s);

        // 字符串常量池：内容相同的字面量指向同一个对象
        String a = "abc";
        String b = "abc";
        System.out.println("\"abc\" == \"abc\" ? " + (a == b));  // true，常量池复用

        // new 出来的在堆上，不走常量池
        String c = new String("abc");
        System.out.println("\"abc\" == new String ? " + (a == c));  // false
        System.out.println("\"abc\".equals(new) ? " + a.equals(c)); // true，内容相同

        // 编译期常量折叠
        String d = "ab" + "c";  // 编译器优化成 "abc"
        System.out.println("\"ab\"+\"c\" == \"abc\" ? " + (d == a));  // true
    }

    /**
     * StringBuilder：可变字符串，线程不安全，性能最好
     * 单线程环境下拼接字符串首选
     */
    private static void stringBuilderDemo() {
        System.out.println("\n===== 2. StringBuilder（可变，不安全） =====");

        StringBuilder sb = new StringBuilder("Hello");
        int hashBefore = System.identityHashCode(sb);

        sb.append(" World");
        sb.insert(5, ",");
        sb.replace(0, 5, "Hi");
        sb.delete(2, 3);
        sb.reverse();

        int hashAfter = System.identityHashCode(sb);
        System.out.println("操作后: " + sb);
        System.out.println("同一个对象？ " + (hashBefore == hashAfter));  // true，没变

        // 链式调用（append 返回 this）
        StringBuilder sb2 = new StringBuilder()
                .append("Java")
                .append(" ")
                .append(21);
        System.out.println("链式: " + sb2);
    }

    /**
     * StringBuffer：可变字符串，线程安全（方法加了 synchronized）
     * API 和 StringBuilder 几乎一模一样，就是慢一点
     */
    private static void stringBufferDemo() {
        System.out.println("\n===== 3. StringBuffer（可变，安全） =====");

        StringBuffer sbf = new StringBuffer("Hello");
        sbf.append(" World");
        sbf.insert(5, ",");
        System.out.println("操作后: " + sbf);

        System.out.println("和 StringBuilder 的唯一区别：方法加了 synchronized，多线程安全但更慢");
    }

    /**
     * 性能对比
     */
    private static void performanceCompare() {
        System.out.println("\n===== 4. 性能对比（循环拼接 10万次） =====");
        int n = 100_000;

        // String 拼接
        long start = System.currentTimeMillis();
        String s = "";
        for (int i = 0; i < n; i++) {
            s = s + "a";  // 每次 new StringBuilder → append → toString
        }
        long stringTime = System.currentTimeMillis() - start;

        // StringBuilder
        start = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("a");
        }
        String sbResult = sb.toString();
        long sbTime = System.currentTimeMillis() - start;

        // StringBuffer
        start = System.currentTimeMillis();
        StringBuffer sbf = new StringBuffer();
        for (int i = 0; i < n; i++) {
            sbf.append("a");
        }
        String sbfResult = sbf.toString();
        long sbfTime = System.currentTimeMillis() - start;

        System.out.println("String      : " + stringTime + " ms");
        System.out.println("StringBuilder: " + sbTime + " ms");
        System.out.println("StringBuffer : " + sbfTime + " ms");
        System.out.println("\nString 最慢，因为每次 + 都创建新对象");
        System.out.println("StringBuilder ≈ StringBuffer，但没有 synchronized 开销");
    }
}
