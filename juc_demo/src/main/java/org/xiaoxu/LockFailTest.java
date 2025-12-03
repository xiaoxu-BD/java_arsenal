package org.xiaoxu;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class LockFailTest {
    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        // 主线程先拿锁，确保异步线程拿不到
        lock.lock();
        System.out.println("主线程已持有锁");

        // 异步线程尝试获取锁
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("异步线程尝试获取锁...");
            boolean acquired = false;
            try {
                // 尝试获取锁，设置超时时间为1秒
                acquired = lock.tryLock(1, TimeUnit.SECONDS);
                if (!acquired) {
                    // 获取锁失败，直接抛异常
                    throw new RuntimeException("异步线程获取锁失败!");
                }
                System.out.println("异步线程成功获取锁");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                if (acquired) {
                    lock.unlock();
                }
            }
        });

        // 主线程等待3秒后释放锁
        Thread.sleep(3000);
        lock.unlock();
        System.out.println("主线程释放锁");

        // 阻塞等待异步线程完成，查看异常
        try {
            future.join();
        } catch (Exception e) {
            System.out.println("捕获到异步线程异常: " + e.getCause().getMessage());
        }
    }
}
