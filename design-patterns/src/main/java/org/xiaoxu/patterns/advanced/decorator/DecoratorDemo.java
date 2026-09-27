package org.xiaoxu.patterns.advanced.decorator;

import java.util.Random;

/**
 * 装饰器组合演示（高级版）
 *
 * <p>业务拿到的 Storage 可以按需拼装：要不要日志？要不要重试？顺序谁先谁后？
 * 拼装方式改变时，MemoryStorage 和各个装饰器类都不用改一行。
 */
public class DecoratorDemo {

    public static void main(String[] args) {
        // 模拟网络抖动：前 2 次写入必失败，第 3 次成功 —— 正好考验 RetryStorage
        FlakyStorage flaky = new FlakyStorage(new MemoryStorage(), 2);
        Storage storage = new LoggingStorage(new RetryStorage(flaky));

        System.out.println("========== 组合：日志(外) + 重试(中) + 抖动存储(内) ==========");
        storage.save("user:1001", "张三");
        System.out.println("读回: " + storage.read("user:1001"));

        System.out.println("\n========== 换一种拼法：只加日志，不加重试 ==========");
        Storage plain = new LoggingStorage(new MemoryStorage());
        plain.save("order:2001", "已支付");

        System.out.println("\n========== 像剥洋葱：最外层日志看到的是重试后的最终结果 ==========");
        // 执行顺序（save 方向）：LoggingStorage -> RetryStorage(失败重试) -> Flaky -> Memory
        // Java IO 的 BufferedReader(new InputStreamReader(new FileInputStream(...))) 同款结构
    }

    /** 测试道具：前 failCount 次写入抛异常，模拟不稳定网络 */
    static class FlakyStorage extends StorageDecorator {

        private final Random random = new Random(42);
        private final int failCount;
        private int invoked = 0;

        FlakyStorage(Storage delegate, int failCount) {
            super(delegate);
            this.failCount = failCount;
        }

        @Override
        public void save(String key, String value) {
            invoked++;
            if (invoked <= failCount) {
                throw new RuntimeException("模拟网络抖动 #" + invoked + " (random=" + random.nextInt(100) + ")");
            }
            delegate.save(key, value);
        }

        @Override
        public String read(String key) {
            return delegate.read(key);
        }
    }
}
