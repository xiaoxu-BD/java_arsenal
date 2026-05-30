package org.xiaoxu.se.foundation;

import java.util.*;

/**
 * Iterable & Iterator 学习案例
 */
public class IterableAndIterator {

    // ======================== 案例1：实现 Iterable 让自定义类支持 for-each ========================

    /**
     * 自定义数字范围类，实现 Iterable 支持 for-each 遍历
     */
    static class NumberRange implements Iterable<Integer> {
        private final int start;
        private final int end;

        public NumberRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<>() {
                private int current = start;

                @Override
                public boolean hasNext() {
                    return current < end;
                }

                @Override
                public Integer next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    return current++;
                }
            };
        }
    }

    // ======================== 案例2：实现 Iterator 进行遍历时安全删除 ========================

    /**
     * 使用 Iterator 遍历并安全删除满足条件的元素
     */
    static List<String> removeByIterator(List<String> list, String target) {
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()){
            if (target.equals(iterator.next())){
                iterator.remove();
            }
        }
        return list;
    }

    // ======================== 案例3：自定义双向迭代器 ========================

    /**
     * 自定义双向链表，支持正向和反向迭代
     */
    static class DoublyLinkedList<T> implements Iterable<T> {
        private Node<T> head;
        private Node<T> tail;
        private int size;

        private static class Node<T> {
            T data;
            Node<T> prev, next;
            Node(T data) { this.data = data; }
        }

        public void add(T data) {
            Node<T> node = new Node<>(data);
            if (tail == null) {
                head = tail = node;
            } else {
                tail.next = node;
                node.prev = tail;
                tail = node;
            }
            size++;
        }

        public int size() { return size; }

        /** 正向迭代 */
        @Override
        public Iterator<T> iterator() {
            return new Iterator<>() {
                private Node<T> current = head;

                @Override
                public boolean hasNext() { return current != null; }

                @Override
                public T next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    T data = current.data;
                    current = current.next;
                    return data;
                }
            };
        }

        /** 反向迭代 */
        public Iterator<T> descendingIterator() {
            return new Iterator<>() {
                private Node<T> current = tail;

                @Override
                public boolean hasNext() { return current != null; }

                @Override
                public T next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    T data = current.data;
                    current = current.prev;
                    return data;
                }
            };
        }
    }

    // ======================== 案例4：惰性求值迭代器（斐波那契数列） ========================

    /**
     * 无限斐波那契数列，按需生成，不会撑爆内存
     */
    static class Fibonacci implements Iterable<Long> {
        private final int limit;

        public Fibonacci(int limit) {
            this.limit = limit;
        }

        @Override
        public Iterator<Long> iterator() {
            return new Iterator<>() {
                private int count = 0;
                private long a = 0, b = 1;

                @Override
                public boolean hasNext() { return count < limit; }

                @Override
                public Long next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    count++;
                    long result = a;
                    long next = a + b;
                    a = b;
                    b = next;
                    return result;
                }
            };
        }
    }

    // ======================== main 方法运行所有案例 ========================

    public static void main(String[] args) {

        // 案例1：NumberRange for-each 遍历
        System.out.println("===== 案例1：自定义 Iterable 支持 for-each =====");

//        List
        for (int n : new NumberRange(1, 6)) {
            System.out.print(n + " ");
        }
        System.out.println();

        // 案例2：Iterator 安全删除
        System.out.println("\n===== 案例2：Iterator 安全删除 =====");
        List<String> fruits = new ArrayList<>(List.of("apple", "banana", "cherry", "banana"));
        System.out.println("删除前: " + fruits);
        removeByIterator(fruits, "banana");
        System.out.println("删除后: " + fruits);

        // 案例3：双向链表正向/反向迭代
        System.out.println("\n===== 案例3：双向链表正向/反向迭代 =====");
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");

        System.out.print("正向: ");
        for (String s : list) {
            System.out.print(s + " ");
        }
        System.out.println();

        System.out.print("反向: ");
        Iterator<String> desc = list.descendingIterator();
        while (desc.hasNext()) {
            System.out.print(desc.next() + " ");
        }
        System.out.println();

        // 案例4：斐波那契惰性迭代
        System.out.println("\n===== 案例4：斐波那契惰性迭代（前10个） =====");
        for (long fib : new Fibonacci(10)) {
            System.out.print(fib + " ");
        }
        System.out.println();
    }
}
