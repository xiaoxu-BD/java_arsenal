package org.xiaoxu;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class AsyncLockFailDemo {
    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        // 主线程先拿锁，保证异步线程拿不到
        lock.lock();
        System.out.println("主线程已持有锁");

        // 异步线程尝试获取锁
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("异步线程尝试获取锁...");
            boolean acquired = false;

            try {
                acquired = lock.tryLock(1, TimeUnit.SECONDS);
                if (!acquired) {
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

//        // 使用 whenComplete 打印异常信息（不阻塞主线程）
//        future.whenComplete((res, ex) -> {
//            if (ex != null) {
//                System.out.println("异步线程异常: " + ex.getCause().getMessage());
//            }
//        });
        future.exceptionally(ex -> {
            System.out.println("异步线程异常: " + ex.getCause().getMessage());
            return null;
        });

        // 主线程释放锁
        Thread.sleep(2000);
        lock.unlock();
        System.out.println("主线程释放锁");

        // 主线程继续做自己的事情
        System.out.println("主线程继续执行其他任务...");

        // 为了看到异步输出，主线程稍微睡一下
        Thread.sleep(2000);
    }
}
