package org.xiaoxu.se.foundation;

/**
 * 值传递深入：从 JVM 栈帧 + JMM 角度理解
 *
 * 核心结论：
 * 1. 方法参数存在线程私有的栈帧局部变量表中 → 天然线程隔离
 * 2. 传参 = 拷贝 slot 里的值（基本类型拷贝数值，对象类型拷贝引用地址）
 * 3. JMM 管的是堆上的共享数据，不管栈帧里的局部变量
 */
public class PassbyValueDeep {

    // =====================================================
    // 一、基本类型：拷贝的是字面值
    // =====================================================
    static void demoPrimitive() {
        int a = 10;
        modifyPrimitive(a);
        // a 还是 10，因为 modifyPrimitive 拷贝了 a 的值（10）
        // 改的是它自己栈帧 slot 里的副本
        System.out.println("[基本类型] a = " + a);  // 10
    }

    static void modifyPrimitive(int x) {
        // x 是 main 栈帧里 a=10 的拷贝，存在当前栈帧的 slot[0]
        x = 100;
        // 只改了当前栈帧 slot[0] 的值，调用者的 slot 不受影响
    }

    // =====================================================
    // 二、对象引用：拷贝的是地址值
    // =====================================================
    static void demoReference() {
        int[] arr = {1, 2, 3};  // arr 存在栈帧 slot 里，值是数组对象在堆上的地址

        modifyContent(arr);     // 拷贝 arr 的地址值传进去 → 两个引用指向同一个堆对象
        System.out.println("[改内容] arr[0] = " + arr[0]);  // 999 ← 堆上的数据被改了

        reassign(arr);          // 拷贝 arr 的地址值传进去
        System.out.println("[改引用] arr[0] = " + arr[0]);  // 999 ← 没变！
    }

    static void modifyContent(int[] a) {
        // a 和 main 的 arr 存的是同一个地址值，指向堆上同一个数组
        a[0] = 999;  // 通过地址找到堆上的数组，改了元素 → 两边都能看到
    }

    static void reassign(int[] a) {
        // a 是 arr 地址值的拷贝
        a = new int[]{7, 8, 9};  // 当前栈帧 slot 里的 a 改指向新对象
        // main 的 arr 仍然指向原来的数组，不受影响
    }

    // =====================================================
    // 三、JMM 角度：栈帧隔离 vs 堆共享
    // =====================================================
    static void demoJMM() throws InterruptedException {
        SharedData data = new SharedData();
        data.value = 0;

        // 线程通过引用修改堆上的对象 → 涉及 JMM 可见性问题
        Thread writer = new Thread(() -> {
            data.value = 42;             // 写堆上的字段
            // 没有 volatile/synchronized，主线程可能看不到这个修改
        });

        writer.start();
        writer.join();  // join 建立了 happens-before，保证可见

        System.out.println("[JMM] data.value = " + data.value);  // 42

        // 但如果把 data 作为方法参数传进去呢？
        // 参数是栈帧里的局部变量（引用的拷贝），线程私有，JMM 不管它
        // JMM 管的是：通过引用访问堆上 data.value 时的可见性
        demoJMMMethodArg(data);
    }

    static void demoJMMMethodArg(SharedData d) {
        // d 是 data 引用的拷贝，指向堆上同一个对象
        // 修改 d.value = 修改 data.value → 这是堆操作，受 JMM 约束
        d.value = 100;
        // 但如果 d = new SharedData() → 只改了当前栈帧的 slot，不影响调用者
        // 这是栈操作，JMM 不管
    }

    // =====================================================
    // 四、多线程下验证：参数拷贝 vs 堆共享
    // =====================================================
    static void demoThreadIsolation() throws InterruptedException {
        int[] counter = {0};  // 堆上的数组对象

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                counter[0]++;  // 操作堆上对象 → 有竞态条件！
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                counter[0]++;  // 同一个堆对象，没有同步
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // 结果 < 20000，因为 counter[0]++ 不是原子操作
        // 堆上的数据被多线程共享 → JMM 的可见性 + 原子性问题
        System.out.println("[线程竞态] counter[0] = " + counter[0]
                + " (期望 20000，实际 < 20000)");

        // 但如果用基本类型做参数呢？
        int local = 0;
        Thread t3 = new Thread(() -> {
            int copy = local;  // 拷贝了 local 的值到线程栈帧
            copy++;            // 改的是线程自己的栈帧 slot
            // 不会影响主线程的 local → 栈帧隔离，没有竞态
        });
        t3.start();
        t3.join();
        System.out.println("[栈帧隔离] local = " + local);  // 0，没变
    }

    static class SharedData {
        int value;
    }

    // =====================================================
    // main
    // =====================================================
    public static void main(String[] args) throws InterruptedException {
        System.out.println("===== 一、基本类型：值拷贝 =====");
        demoPrimitive();

        System.out.println("\n===== 二、引用类型：地址值拷贝 =====");
        demoReference();

        System.out.println("\n===== 三、JMM 与栈帧 =====");
        demoJMM();

        System.out.println("\n===== 四、多线程：堆竞态 vs 栈隔离 =====");
        demoThreadIsolation();
    }
}
