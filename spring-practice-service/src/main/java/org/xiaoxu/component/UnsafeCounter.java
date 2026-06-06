package org.xiaoxu.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @className: UnsafeCounter
 * @description: 有状态的单例Bean，演示线程安全问题
 */
@Slf4j
@Component
public class UnsafeCounter {

    // 这个实例变量就是"状态"，所有线程共享同一个count
    private int count = 0;

    public int increment() {
        // 模拟一些耗时操作，放大并发冲突的概率
        int temp = count;
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        count = temp + 1;
        return count;
    }

    public int getCount() {
        return count;
    }

    public void reset() {
        count = 0;
    }
}
