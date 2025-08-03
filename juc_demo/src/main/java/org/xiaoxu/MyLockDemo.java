package org.xiaoxu;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * AQS 实现一个简单的锁
 */
public class MyLockDemo {

    static class MyLock extends AbstractQueuedSynchronizer {
        // 获取锁
        @Override
        protected boolean tryAcquire(int arg) {
            // state == 0 表示锁空闲，CAS 尝试拿锁
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                System.out.println(Thread.currentThread().getName() + " 获取锁成功");
                return true;
            }
            System.out.println(Thread.currentThread().getName() + " 获取锁失败");
            return false;
        }

        // 释放锁
        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == 0) throw new IllegalMonitorStateException();
            setExclusiveOwnerThread(null);
            setState(0);
            System.out.println(Thread.currentThread().getName() + " 释放锁");
            return true;
        }

        public void lock() {
            acquire(1);
        }

        public void unlock() {
            release(1);
        }
    }

    public static void main(String[] args) {
        MyLock lock = new MyLock();

        Runnable task = () -> {
            System.out.println(Thread.currentThread().getName() + " 尝试获取锁");
            lock.lock(); // 会调用 AQS.acquire()
            try {
                System.out.println(Thread.currentThread().getName() + " 执行中...");
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {}
            lock.unlock();
        };

        // 启动两个线程，第二个将被阻塞
        new Thread(task, "线程-T1").start();
        new Thread(task, "线程-T2").start();
    }
}
