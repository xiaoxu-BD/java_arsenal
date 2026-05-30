package org.xiaoxu.se.collection;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HashMap 深度学习 — 12 个核心问题逐一拆解
 */
public class HashMapStudy {

    // ======================== Q1: 底层数据结构 ========================

    /**
     * JDK8 HashMap 结构：
     *
     * 数组 + 链表 + 红黑树
     *
     *  table (Node[])
     *  ┌───┐
     *  │ 0 │ → null
     *  ├───┤
     *  │ 1 │ → Node(k1,v1) → Node(k5,v5) → null       ← 链表（≤8个节点）
     *  ├───┤
     *  │ 2 │ → TreeNode(k2,v2)                          ← 红黑树（>8个节点）
     *  │   │       │
     *  │   │    ┌──┴──┐
     *  │   │  TreeNode TreeNode
     *  ├───┤
     *  │ 3 │ → Node(k3,v3) → null
     *  ├───┤
     *  │...│
     *  └───┘
     *
     *  核心字段：
     *  - table: Node[] 数组，长度永远是 2 的幂
     *  - size: 实际存储的 KV 对数量
     *  - threshold: 扩容阈值 = capacity * loadFactor
     *  - loadFactor: 负载因子，默认 0.75
     */

    // ======================== Q2: put 一个元素经历了哪些步骤 ========================

    /**
     * put(key, value) 完整流程：
     *
     * 1. table 为空 → resize() 初始化
     * 2. 计算 key 的 hash 值
     * 3. 用 hash & (n-1) 定位桶下标
     * 4. 桶为空 → 直接放新 Node
     * 5. 桶不为空：
     *    a. 首节点 key 相同 → 覆盖 value
     *    b. 首节点是 TreeNode → 红黑树插入
     *    c. 遍历链表：
     *       - 找到相同 key → 覆盖 value
     *       - 没找到 → 尾部追加新 Node
     *       - 追加后链表长度 > 8 → 树化
     * 6. size > threshold → resize() 扩容
     */
    static void demonstratePutProcess() {
        System.out.println("===== Q2: put 过程演示 =====\n");

        HashMap<String, Integer> map = new HashMap<>(4);

        // 步骤1: 首次 put，触发 resize 初始化 table
        System.out.println("put(\"a\", 1): 首次插入，初始化 table");
        map.put("a", 1);

        // 步骤2-4: 计算 hash → 定位桶 → 放入
        System.out.println("put(\"b\", 2): hash 定位桶，桶为空直接放");
        map.put("b", 2);

        // 步骤5a: key 相同，覆盖 value
        System.out.println("put(\"a\", 99): key 相同，覆盖旧值");
        map.put("a", 99);
        System.out.println("  map.get(\"a\") = " + map.get("a"));  // 99

        // 步骤5c: 链表追加
        System.out.println("put(\"c\", 3): 链表追加新节点");
        map.put("c", 3);

        System.out.println("\n最终 map: " + map);
    }

    // ======================== Q3: hash 方法实现 ========================

    /**
     * JDK8 的 hash 方法：
     *
     *   static final int hash(Object key) {
     *       int h;
     *       return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
     *   }
     *
     * 为什么要这样做？
     *
     * hashCode() 返回 32 位 int，但 HashMap 的桶数通常很小（16、32...）
     * 如果直接用 hashCode() & (n-1) 取低位，高位信息完全丢失
     * 高位不同但低位相同的 key 会大量冲突
     *
     * 解决方案：高 16 位 XOR 低 16 位 → 高位特征混入低位 → 减少冲突
     *
     *   h =    1111 1111 0000 0000 1111 0000 1010 1010
     *   h>>>16 =                        1111 1111 0000 0000
     *   XOR    =    1111 1111 0000 0000 0000 1111 1010 1010
     *                                        ^^^^
     *                                        ^^^^^^^^
     *                              低位现在混合了高位特征
     */
    static void demonstrateHash() {
        System.out.println("===== Q3: hash 方法解析 =====\n");

        String key = "testKey";
        int h = key.hashCode();
        int hash = h ^ (h >>> 16);

        System.out.println("key.hashCode() = " + Integer.toBinaryString(h));
        System.out.println("h >>> 16       = " + Integer.toBinaryString(h >>> 16));
        System.out.println("hash (XOR)     = " + Integer.toBinaryString(hash));
        System.out.println();

        // 对比：不混入高位 vs 混入高位，桶定位结果
        int capacity = 16;
        int mask = capacity - 1;  // 0b1111
        System.out.println("capacity=16, mask=" + Integer.toBinaryString(mask));
        System.out.println("  直接用 hashCode: 桶下标 = " + (h & mask));
        System.out.println("  用 hash (XOR后): 桶下标 = " + (hash & mask));

        // 展示不同 key 的高位不同但低位相同时的情况
        System.out.println("\n高位不同但低位相同的 key：");
        int k1 = 0b1111_0000_0000_0000_0000_0000_0000_0101;  // 低位 0101
        int k2 = 0b0000_1111_0000_0000_0000_0000_0000_0101;  // 低位也是 0101
        System.out.println("  k1 & mask = " + (k1 & mask) + ", k2 & mask = " + (k2 & mask) + " → 不做 XOR 会冲突!");
        int h1 = k1 ^ (k1 >>> 16);
        int h2 = k2 ^ (k2 >>> 16);
        System.out.println("  hash(k1) & mask = " + (h1 & mask) + ", hash(k2) & mask = " + (h2 & mask) + " → XOR 后分散了");
    }

    // ======================== Q4: 为什么用 & 不用 % ========================

    /**
     * hash % n  → 需要除法运算，CPU 执行很慢
     * hash & (n-1) → 纯位运算，一个时钟周期搞定
     *
     * 前提：n 必须是 2 的幂，这样 n-1 的二进制全是 1，& 才等价于 %
     */
    static void demonstrateBitwiseVsMod() {
        System.out.println("===== Q4: & 与 % 的等价性 =====\n");

        int n = 16;
        int mask = n - 1;  // 15 = 0b1111

        System.out.println("n=16, mask=15 (0b1111)");
        System.out.println("验证 & 等价于 %:");
        for (int hash : new int[]{0, 5, 15, 16, 17, 31, 33, 100}) {
            int byMod = hash % n;
            int byAnd = hash & mask;
            String match = byMod == byAnd ? "✓" : "✗";
            System.out.println("  hash=" + hash + "  % " + n + "=" + byMod + "  & " + mask + "=" + byAnd + "  " + match);
        }

        // 性能对比
        System.out.println("\n性能对比 (1亿次运算):");
        int iterations = 100_000_000;
        long sum;

        long start = System.nanoTime();
        sum = 0;
        for (int i = 0; i < iterations; i++) {
            sum += i % 16;
        }
        long modTime = System.nanoTime() - start;

        start = System.nanoTime();
        sum = 0;
        for (int i = 0; i < iterations; i++) {
            sum += i & 15;
        }
        long andTime = System.nanoTime() - start;

        System.out.println("  % 16 耗时: " + modTime / 1_000_000 + " ms");
        System.out.println("  & 15 耗时: " + andTime / 1_000_000 + " ms");
        System.out.println("  & 快 " + (modTime / Math.max(andTime, 1)) + " 倍");
    }

    // ======================== Q5: 为什么容量必须是 2 的幂 ========================

    /**
     * 2 的幂 - 1 的二进制全是 1：
     *   16 - 1 = 15 = 0b1111
     *   32 - 1 = 31 = 0b11111
     *
     * 这样 hash & (n-1) 的结果范围是 [0, n-1]，且每一位都能参与运算
     * 如果 n 不是 2 的幂，比如 n=10，n-1=9=0b1001
     * hash & 0b1001 的结果只能是 0,1,8,9，桶 2-7 永远不会被用到！
     */
    static void demonstratePowerOfTwo() {
        System.out.println("===== Q5: 2 的幂 vs 非 2 的幂 =====\n");

        // 2 的幂：n=16, mask=15=0b1111，所有桶都能命中
        System.out.println("n=16 (2的幂), mask=0b1111:");
        Map<Integer, Integer> bucketCount16 = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            int bucket = i & 15;
            bucketCount16.merge(bucket, 1, Integer::sum);
        }
        System.out.println("  1000个hash值落入桶分布: " + bucketCount16);
        System.out.println("  使用的桶数: " + bucketCount16.size() + "/16");

        // 非 2 的幂：n=10, mask=9=0b1001，大量桶浪费
        System.out.println("\nn=10 (非2的幂), mask=0b1001:");
        Map<Integer, Integer> bucketCount10 = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            int bucket = i & 9;
            bucketCount10.merge(bucket, 1, Integer::sum);
        }
        System.out.println("  1000个hash值落入桶分布: " + bucketCount10);
        System.out.println("  使用的桶数: " + bucketCount10.size() + "/10  (大量桶浪费!)");
    }

    // ======================== Q6: initialCapacity=10 实际容量是多少 ========================

    /**
     * tableSizeFor(cap) 方法：找到 ≥ cap 的最小 2 的幂
     *
     * 算法：把 cap-1 的二进制从最高位 1 开始，右边全部填 1，再 +1
     *
     * cap=10:
     *   cap - 1 = 9  = 0b0000_1001
     *   >>> 1       = 0b0000_0100   n |= n >>> 1
     *   OR          = 0b0000_1101
     *   >>> 2       = 0b0000_0011   n |= n >>> 2
     *   OR          = 0b0000_1111
     *   >>> 4       = 0b0000_0000   n |= n >>> 4
     *   OR          = 0b0000_1111
     *   ...同理更高位不变
     *   最终 n = 0b0000_1111 = 15
     *   n + 1 = 16
     *
     * 所以 initialCapacity=10 → 实际容量 16
     * 但注意：threshold = 16 * 0.75 = 12
     */
    static void demonstrateTableSizeFor() {
        System.out.println("===== Q6: initialCapacity=10 实际容量 =====\n");

        // 手动模拟 tableSizeFor
        for (int cap : new int[]{1, 2, 3, 5, 9, 10, 16, 17, 100}) {
            int result = tableSizeFor(cap);
            System.out.println("  initialCapacity=" + cap + " → 实际容量=" + result);
        }

        // 验证：用反射看 HashMap 内部实际值
        System.out.println("\n反射验证 HashMap(10) 的内部字段:");
        HashMap<String, Integer> map = new HashMap<>(10);
        map.put("a", 1);  // 触发初始化
        printHashMapInternalFields(map);

        // 注意：直接 new HashMap<>(10) 时 capacity 还没初始化
        // 首次 put 触发 resize，capacity = threshold
        // 而 threshold 在构造器中被 tableSizeFor 设为 16
        System.out.println("\n所以 initialCapacity=10 → 实际 table 长度=16, threshold=12");
    }

    // ======================== Q7: 扩容时元素怎么重新分布 ========================

    /**
     * 扩容 resize() 核心逻辑：
     *
     * 新容量 = 旧容量 * 2
     * 元素在新表中的位置只有两种可能：
     *   1. 原位置
     *   2. 原位置 + 旧容量
     *
     * 判断依据：hash & oldCap
     *   - 结果为 0 → 位置不变
     *   - 结果不为 0 → 位置 = 原位置 + oldCap
     *
     * 为什么？因为容量翻倍，mask 多了最高一位 1
     *   旧 mask: 0b0111 (cap=8)
     *   新 mask: 0b1111 (cap=16)
     *   多出的那一位是 0 还是 1 决定了位置是否变化
     */
    static void demonstrateResize() {
        System.out.println("===== Q7: 扩容时元素重新分布 =====\n");

        int oldCap = 8;
        int newCap = 16;
        int oldMask = oldCap - 1;  // 0b0111
        int newMask = newCap - 1;  // 0b1111

        System.out.println("旧容量=" + oldCap + " (mask=0b" + Integer.toBinaryString(oldMask) + ")");
        System.out.println("新容量=" + newCap + " (mask=0b" + Integer.toBinaryString(newMask) + ")");
        System.out.println();

        // 模拟 16 个 hash 值的重新分布
        System.out.println("hash值    | 旧桶位(hash&7) | hash&oldCap | 新桶位(hash&15) | 是否移动");
        System.out.println("----------|---------------|-------------|-----------------|--------");

        for (int hash : new int[]{3, 5, 7, 8, 10, 11, 15, 16, 17, 19, 23, 24}) {
            int oldPos = hash & oldMask;
            int bit = hash & oldCap;       // 关键：检查多出的那一位
            int newPos = hash & newMask;
            String moved = bit == 0 ? "不动" : "移动 +" + oldCap;
            System.out.printf("  %5d   |      %2d       |     %2d      |      %2d         | %s%n",
                    hash, oldPos, bit, newPos, moved);
        }

        System.out.println("\n规律：hash & oldCap == 0 的留在原位，!= 0 的移到 原位+oldCap");
        System.out.println("不需要重新计算 hash，只需要看多出的那一位是 0 还是 1");
    }

    // ======================== Q8: 链表转红黑树的条件 ========================

    /**
     * 链表 → 红黑树的条件（同时满足）：
     *   1. 链表长度 > 8
     *   2. 数组长度 >= 64
     *
     * 如果链表长度 > 8 但数组长度 < 64，优先扩容而不是树化
     *
     * 为什么是 8？
     * 泊松分布：在默认负载因子 0.75 下，一个桶中链表长度达到 8 的概率约为 0.00000006（千万分之六）
     * 这是一个极小概率事件，说明 hash 函数分布已经非常不均匀了
     *
     *   长度    概率
     *   0       0.60653066
     *   1       0.30326533
     *   2       0.07581633
     *   3       0.01263606
     *   4       0.00157951
     *   5       0.00015795
     *   6       0.00001316
     *   7       0.00000094
     *   8       0.00000006   ← 百万分之一都不到
     */
    static void demonstrateTreeify() {
        System.out.println("===== Q8: 链表转红黑树 =====\n");

        // 泊松分布验证：模拟大量插入，观察链表长度分布
        System.out.println("模拟泊松分布（100万个元素，1024个桶，负载因子≈0.977）:");
        int buckets = 1024;
        int elements = 1_000_000;
        int[] chainLength = new int[20];  // 统计每个长度的桶数量

        // 模拟：每个元素随机落入某个桶
        Random rand = new Random(42);
        int[] bucketSizes = new int[buckets];
        for (int i = 0; i < elements; i++) {
            bucketSizes[rand.nextInt(buckets)]++;
        }
        for (int size : bucketSizes) {
            if (size < chainLength.length) {
                chainLength[size]++;
            }
        }

        System.out.println("  链表长度 | 桶数量  | 占比");
        for (int len = 0; len <= 10; len++) {
            if (chainLength[len] > 0) {
                double pct = chainLength[len] * 100.0 / buckets;
                System.out.printf("     %2d    | %5d   | %.2f%%%n", len, chainLength[len], pct);
            }
        }

        System.out.println("\n结论：长度达到 8 的桶极少，说明此时 hash 冲突已经很严重");
        System.out.println("树化阈值 8 是在空间（红黑树额外开销）和时间（O(n)→O(logn)）之间的权衡");

        // 树化还需要数组长度 >= 64
        System.out.println("\n条件二：数组长度 >= 64");
        System.out.println("如果数组很小（如 16），优先扩容（扩容可以分散元素到更多桶）");
        System.out.println("只有数组已经足够大了，再扩容也没用时，才树化");
    }

    // ======================== Q9: 为什么用红黑树不用 AVL 树 ========================

    /**
     * AVL 树：严格平衡，任意节点左右子树高度差 ≤ 1
     *   查找 O(log n) 最优
     *   插入/删除需要频繁旋转（最多 O(log n) 次旋转）来维持平衡
     *
     * 红黑树：近似平衡，最长路径不超过最短路径 2 倍
     *   查找 O(log n) 稍慢（但常数差别很小）
     *   插入/删除最多 2-3 次旋转 → 适合频繁插入删除的场景
     *
     * HashMap 中的链表节点经常增删，红黑树的旋转代价更低
     * 且红黑树节点只需 1 个额外 bit 存颜色，AVL 需要存高度差
     *
     *   特性           AVL树          红黑树
     *   平衡           严格平衡        近似平衡
     *   查找           O(logn) 最优    O(logn) 稍慢
     *   插入旋转       最多O(logn)次   最多2次
     *   删除旋转       最多O(logn)次   最多3次
     *   实际查找深度   更浅            略深但差距小
     *   适用场景       读多写少        读写均衡
     */
    static void demonstrateRedBlackVsAVL() {
        System.out.println("===== Q9: 红黑树 vs AVL 树 =====\n");

        // 用 TreeSet（红黑树）模拟 HashMap 桶中的树操作
        TreeMap<Integer, String> rbTree = new TreeMap<>();
        Random rand = new Random(42);

        long start = System.nanoTime();
        for (int i = 0; i < 100_000; i++) {
            rbTree.put(rand.nextInt(1_000_000), "v" + i);
        }
        long insertTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < 100_000; i++) {
            rbTree.get(rand.nextInt(1_000_000));
        }
        long searchTime = System.nanoTime() - start;

        System.out.println("TreeMap (红黑树) 10万次操作:");
        System.out.println("  插入: " + insertTime / 1_000_000 + " ms");
        System.out.println("  查找: " + searchTime / 1_000_000 + " ms");
        System.out.println("\n红黑树选择理由：HashMap 场景写操作频繁（put/remove），红黑树旋转代价更低");
    }

    // ======================== Q10: 多线程下会有什么问题 ========================

    /**
     * JDK7 HashMap 多线程两大致命问题：
     *
     * 1. 死循环（CPU 100%）— 扩容时头插法导致环形链表
     *    线程1 在扩容移动链表 A→B→C
     *    线程2 同时也在扩容，头插法导致 C→B→A，但 B.next 还指向 A
     *    结果：A→B→A 形成环，get() 时无限循环
     *
     * 2. 数据丢失 — 并发 put 覆盖彼此的写入
     *
     * JDK8 修复了死循环（改为尾插法），但仍有问题：
     * 1. 数据丢失 — 并发 put 仍可能丢数据
     * 2. size 不准确 — size++ 不是原子操作
     */
    static void demonstrateMultiThreadProblem() {
        System.out.println("===== Q10: 多线程问题演示 =====\n");

        // 演示并发 put 数据丢失
        HashMap<Integer, Integer> map = new HashMap<>();
        int threadCount = 10;
        int perThread = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int offset = t * perThread;
            new Thread(() -> {
                for (int i = 0; i < perThread; i++) {
                    map.put(offset + i, i);  // 并发 put
                }
                latch.countDown();
            }).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int expected = threadCount * perThread;
        int actual = map.size();
        System.out.println("10个线程各put 1000个元素:");
        System.out.println("  期望: " + expected);
        System.out.println("  实际: " + actual);
        System.out.println("  丢失: " + (expected - actual) + " 个元素!");
    }

    // ======================== Q11: JDK8 之后就完全安全了吗？ ========================

    /**
     * JDK8 修复了死循环，但 HashMap 仍然不是线程安全的：
     *
     * 1. put 数据丢失：两个线程同时 put 到同一个空桶，后写入的覆盖先写入的
     * 2. size 不准确：size++ 是 read-modify-write，非原子操作
     * 3. get 可能返回 null：线程 A put 的值还没刷入主存，线程 B 读不到
     *
     * HashMap 的设计目标就不是线程安全，它是单线程最优解
     */
    static void demonstrateJDK8StillUnsafe() {
        System.out.println("===== Q11: JDK8 HashMap 仍然不安全 =====\n");

        // 验证 size 不准确
        HashMap<Integer, Integer> map = new HashMap<>();
        int threadCount = 10;
        int perThread = 10000;
        AtomicInteger putCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int offset = t * perThread;
            new Thread(() -> {
                for (int i = 0; i < perThread; i++) {
                    map.put(offset + i, i);
                    putCount.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("实际 put 次数: " + putCount.get());
        System.out.println("map.size(): " + map.size());
        System.out.println("size 与实际不一致 = HashMap 非线程安全的直接证据");
    }

    // ======================== Q12: 怎么解决？ ========================

    /**
     * 三种方案：
     *
     * 1. ConcurrentHashMap — 推荐！分段锁/CAS，高并发最优
     * 2. Collections.synchronizedMap — 全局锁，简单但性能差
     * 3. Hashtable — 遗弃类，全表锁，不要用
     */
    static void demonstrateSolutions() {
        System.out.println("===== Q12: 线程安全解决方案 =====\n");

        int threadCount = 10;
        int perThread = 10000;

        // 方案1: ConcurrentHashMap（推荐）
        ConcurrentHashMap<Integer, Integer> concurrentMap = new ConcurrentHashMap<>();
        runConcurrentPut(concurrentMap, threadCount, perThread, "ConcurrentHashMap");

        // 方案2: synchronizedMap
        Map<Integer, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
        runConcurrentPut(syncMap, threadCount, perThread, "synchronizedMap");

        // 方案3: Hashtable（仅作对比，不推荐）
        Hashtable<Integer, Integer> hashtable = new Hashtable<>();
        runConcurrentPut(hashtable, threadCount, perThread, "Hashtable");

        System.out.println("\n结论：高并发场景用 ConcurrentHashMap，它是专为并发设计的");
    }

    // ======================== 辅助方法 ========================

    /**
     * 模拟 JDK8 的 tableSizeFor
     * 找到 ≥ cap 的最小 2 的幂
     */
    static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= 1 << 30) ? 1 << 30 : n + 1;
    }

    /** 反射读取 HashMap 内部字段 */
    static void printHashMapInternalFields(HashMap<?, ?> map) {
        try {
            Field tableField = HashMap.class.getDeclaredField("table");
            Field thresholdField = HashMap.class.getDeclaredField("threshold");
            Field loadFactorField = HashMap.class.getDeclaredField("loadFactor");
            Field sizeField = HashMap.class.getDeclaredField("size");

            tableField.setAccessible(true);
            thresholdField.setAccessible(true);
            loadFactorField.setAccessible(true);
            sizeField.setAccessible(true);

            Object[] table = (Object[]) tableField.get(map);
            int threshold = thresholdField.getInt(map);
            float loadFactor = loadFactorField.getFloat(map);
            int size = sizeField.getInt(map);

            System.out.println("  table.length (capacity) = " + (table == null ? "null(未初始化)" : table.length));
            System.out.println("  threshold = " + threshold);
            System.out.println("  loadFactor = " + loadFactor);
            System.out.println("  size = " + size);
        } catch (Exception e) {
            System.out.println("  反射失败: " + e.getMessage());
        }
    }

    /** 通用并发测试方法 */
    static void runConcurrentPut(Map<Integer, Integer> map, int threadCount, int perThread, String name) {
        long start = System.nanoTime();
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int offset = t * perThread;
            new Thread(() -> {
                for (int i = 0; i < perThread; i++) {
                    map.put(offset + i, i);
                }
                latch.countDown();
            }).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long time = (System.nanoTime() - start) / 1_000_000;
        int expected = threadCount * perThread;
        System.out.println("  " + name + ": size=" + map.size()
                + (map.size() == expected ? " ✓" : " ✗ 丢失" + (expected - map.size()) + "个")
                + ", 耗时=" + time + "ms");
    }

    // ======================== main ========================

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       HashMap 12 个核心问题深度解析       ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

//        demonstratePutProcess();       // Q2
//        System.out.println();

//        demonstrateHash();             // Q3
//        System.out.println();

        demonstrateBitwiseVsMod();     // Q4
        System.out.println();
//
//        demonstratePowerOfTwo();       // Q5
//        System.out.println();
//
//        demonstrateTableSizeFor();     // Q6
//        System.out.println();
//
//        demonstrateResize();           // Q7
//        System.out.println();
//
//        demonstrateTreeify();          // Q8
//        System.out.println();
//
//        demonstrateRedBlackVsAVL();    // Q9
//        System.out.println();
//
//        demonstrateMultiThreadProblem();  // Q10
//        System.out.println();
//
//        demonstrateJDK8StillUnsafe();  // Q11
//        System.out.println();
//
//        demonstrateSolutions();        // Q12
    }
}
