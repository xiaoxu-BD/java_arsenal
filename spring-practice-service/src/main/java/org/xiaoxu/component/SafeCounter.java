package org.xiaoxu.component;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @className: SafeCounter
 * @description: 线程安全的写法，用AtomicInteger替代普通int
 */
@Component
public class SafeCounter {

    private final AtomicInteger count = new AtomicInteger(0);

    public int increment() {
        return count.incrementAndGet();
    }

    public int getCount() {
        return count.get();
    }

    public void reset() {
        count.set(0);
    }
}
