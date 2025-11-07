package org.xiaoxu.state;

public class JUCDEM {

        private static final Object lock = new Object();
        private static boolean ready = false;

        public static void main(String[] args) throws InterruptedException {
            Thread consumer = new Thread(() -> {
                synchronized (lock) {
                    System.out.println("消费者：拿到锁");
                    while (!ready) {
                        System.out.println("消费者：还没准备好，等待...");
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    System.out.println("消费者：开始处理任务！");
                }
            });

            Thread producer = new Thread(() -> {
                synchronized (lock) {
                    System.out.println("生产者：拿到锁，开始准备数据...");
                    try { Thread.sleep(2000); } catch (Exception e) {}
                    ready = true;
                    System.out.println("生产者：数据准备好了，通知消费者");
                    lock.notify();
                    System.out.println("生产者：继续做收尾工作...");
                    try { Thread.sleep(3000); } catch (Exception e) {}
                    System.out.println("生产者：释放锁");
                }
            });

            consumer.start();
            Thread.sleep(500);
            producer.start();
        }
}