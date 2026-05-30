package org.xiaoxu.se.collectiononself;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 手写 LinkedList，理解双向链表的本质结构
 * 为后续学习 HashMap 的链表/红黑树部分打基础
 */
public class CollectionInArrayListAndLinkedList {

    // ======================== 手写 LinkedList 核心结构 ========================

    static class MyLinkedList<E> implements Iterable<E> {

        // ---- 核心字段：只有三个 ----
        private Node<E> first;  // 头节点引用
        private Node<E> last;   // 尾节点引用
        private int size;       // 元素个数

        // ---- 节点定义：prev + item + next ----
        private static class Node<E> {
            Node<E> prev;
            E item;
            Node<E> next;

            Node(Node<E> prev, E item, Node<E> next) {
                this.prev = prev;
                this.item = item;
                this.next = next;
            }

            @Override
            public String toString() {
                return "[" + item + "]";
            }
        }

        // ---- 尾部添加：最常用的操作 ----
        public void addLast(E e) {
            Node<E> l = last;              // 保存当前尾节点
            Node<E> newNode = new Node<>(l, e, null);  // 新节点 prev 指向旧尾
            last = newNode;                // last 指向新节点
            if (l == null) {
                first = newNode;           // 链表为空时，first 也指向新节点
            } else {
                l.next = newNode;          // 旧尾的 next 指向新节点
            }
            size++;
        }

        // ---- 头部添加 ----
        public void addFirst(E e) {
            Node<E> f = first;
            Node<E> newNode = new Node<>(null, e, f);
            first = newNode;
            if (f == null) {
                last = newNode;
            } else {
                f.prev = newNode;
            }
            size++;
        }

        // ---- 获取元素：必须从头/尾遍历，这是和 ArrayList 最大的区别 ----
        public E get(int index) {
            checkIndex(index);
            // 优化：从头还是从尾遍历，取决于哪个更近
            if (index < size / 2) {
                Node<E> node = first;
                for (int i = 0; i < index; i++) {
                    node = node.next;      // 从头向后走 index 步
                }
                return node.item;
            } else {
                Node<E> node = last;
                for (int i = size - 1; i > index; i--) {
                    node = node.prev;      // 从尾向前走 (size-1-index) 步
                }
                return node.item;
            }
        }

        // ---- 删除头节点 ----
        public E removeFirst() {
            if (first == null) throw new NoSuchElementException();
            E item = first.item;
            Node<E> next = first.next;
            first.item = null;             // 帮助 GC
            first.next = null;
            first = next;
            if (next == null) {
                last = null;               // 链表变空了
            } else {
                next.prev = null;
            }
            size--;
            return item;
        }

        // ---- 删除指定节点（HashMap 里删除链表节点就是这个逻辑） ----
        E unlink(Node<E> node) {
            E item = node.item;
            Node<E> prev = node.prev;
            Node<E> next = node.next;

            if (prev == null) {
                first = next;              // node 是头节点
            } else {
                prev.next = next;          // 前驱的 next 跳过 node
                node.prev = null;
            }

            if (next == null) {
                last = prev;               // node 是尾节点
            } else {
                next.prev = prev;          // 后继的 prev 跳过 node
                node.next = null;
            }

            node.item = null;
            size--;
            return item;
        }

        public int size() { return size; }
        public E getFirst() { return first == null ? null : first.item; }
        public E getLast() { return last == null ? null : last.item; }

        private void checkIndex(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
        }

        // ---- Iterator 实现：顺着 next 指针一路走 ----
        @Override
        public Iterator<E> iterator() {
            return new Iterator<>() {
                private Node<E> current = first;

                @Override
                public boolean hasNext() { return current != null; }

                @Override
                public E next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    E item = current.item;
                    current = current.next;
                    return item;
                }
            };
        }

        // ---- 打印链表结构，方便理解指针关系 ----
        public void printStructure() {
            System.out.print("first → ");
            Node<E> node = first;
            while (node != null) {
                System.out.print(node);
                if (node.next != null) System.out.print(" ⇄ ");
                node = node.next;
            }
            System.out.println(" ← last  (size=" + size + ")");
        }
    }

    // ======================== 和 HashMap 的关联：桶中的链表 ========================

    /**
     * HashMap 桶结构简化模型
     * HashMap 的每个桶(bucket) 就是一个链表头
     * 当发生哈希冲突时，冲突的 Entry 按链表串联
     *
     * HashMap 桶数组:
     * [0] → null
     * [1] → Entry(k1,v1) → Entry(k5,v5) → null   ← 哈希冲突，链成一串
     * [2] → Entry(k2,v2) → null
     * [3] → null
     * ...
     */
    static class SimpleHashMapBucket<K, V> {

        // 链表节点（类似 HashMap.Node）
        static class Entry<K, V> {
            final int hash;
            final K key;
            V value;
            Entry<K, V> next;

            Entry(int hash, K key, V value, Entry<K, V> next) {
                this.hash = hash;
                this.key = key;
                this.value = value;
                this.next = next;           // 头插法：新节点指向旧头
            }

            @Override
            public String toString() {
                return "(" + key + ":" + value + ")";
            }
        }

        private Entry<K, V> head;  // 桶头（简化：只展示一个桶）

        // 放入元素（头插法，JDK7 的 HashMap 就是这么干的）
        public void put(K key, V value) {
            int hash = key.hashCode();
            // 头插法：新 Entry 的 next 指向当前 head
            head = new Entry<>(hash, key, value, head);
        }

        // 查找元素：遍历链表逐个比较 key
        public V get(K key) {
            int hash = key.hashCode();
            Entry<K, V> e = head;
            while (e != null) {
                if (e.hash == hash && (e.key == key || e.key.equals(key))) {
                    return e.value;
                }
                e = e.next;                // 沿链表往下找
            }
            return null;
        }

        // 打印桶中的链表
        public void printBucket() {
            System.out.print("bucket → ");
            Entry<K, V> e = head;
            while (e != null) {
                System.out.print(e);
                if (e.next != null) System.out.print(" → ");
                e = e.next;
            }
            System.out.println(" → null");
        }
    }

    // ======================== main：运行所有演示 ========================

    public static void main(String[] args) {

        // ---- 1. 手写 LinkedList 基本操作 ----
        System.out.println("===== 1. 手写 LinkedList 基本操作 =====\n");

        MyLinkedList<String> list = new MyLinkedList<>();
        list.addLast("A");
        list.addLast("B");
        list.addLast("C");
        list.printStructure();               // first → [A] ⇄ [B] ⇄ [C] ← last

        list.addFirst("Z");
        list.printStructure();               // first → [Z] ⇄ [A] ⇄ [B] ⇄ [C] ← last

        System.out.println("get(0)=" + list.get(0) + ", get(2)=" + list.get(2) + ", get(3)=" + list.get(3));
        System.out.println("first=" + list.getFirst() + ", last=" + list.getLast());

        // ---- 2. 演示 get(index) 的遍历过程 ----
        System.out.println("\n===== 2. get(index) 从头/尾遍历定位 =====\n");
        System.out.println("get(0): 从头走 0 步 → " + list.get(0));
        System.out.println("get(1): 从头走 1 步 → " + list.get(1));
        System.out.println("get(3): 从尾走 0 步 → " + list.get(3));

        // ---- 3. 演示删除操作的指针变化 ----
        System.out.println("\n===== 3. 删除操作 =====\n");

        list.removeFirst();
        System.out.println("removeFirst 后:");
        list.printStructure();

        // 遍历输出
        System.out.println("for-each 遍历:");
        for (String s : list) {
            System.out.print(s + " ");
        }
        System.out.println();

        // ---- 4. HashMap 桶中的链表（头插法） ----
        System.out.println("\n\n===== 4. HashMap 桶中链表（头插法演示） =====\n");

        SimpleHashMapBucket<String, Integer> bucket = new SimpleHashMapBucket<>();
        bucket.put("apple", 1);
        bucket.put("banana", 2);
        bucket.put("cherry", 3);
        bucket.printBucket();
        // 注意顺序：cherry → banana → apple（头插法，后插入的在前面）

        System.out.println("\n查找 banana: " + bucket.get("banana"));
        System.out.println("查找不存在: " + bucket.get("grape"));

        // ---- 5. 关键总结 ----
        System.out.println("\n===== 总结：LinkedList 与 HashMap 的关系 =====");
        System.out.println("1. LinkedList 核心 = Node(prev, item, next) 双向链表");
        System.out.println("2. HashMap 桶 = Node(hash, key, value, next) 单向链表");
        System.out.println("3. HashMap 用链表解决哈希冲突：冲突的 KV 按链表串在同一个桶里");
        System.out.println("4. JDK8 优化：桶中链表长度 > 8 时转红黑树，查找从 O(n) → O(log n)");
        System.out.println("5. 理解了 LinkedList 的指针操作，就理解了 HashMap 桶的核心逻辑");
    }
}
