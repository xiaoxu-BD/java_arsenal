package org.xiaoxu.patterns.advanced.decorator;

/** 装饰器：写失败自动重试（网络抖动兜底），超过次数仍失败才向外抛异常 */
public class RetryStorage extends StorageDecorator {

    private final int maxRetries;

    public RetryStorage(Storage delegate) {
        this(delegate, 3);
    }

    public RetryStorage(Storage delegate, int maxRetries) {
        super(delegate);
        this.maxRetries = maxRetries;
    }

    @Override
    public void save(String key, String value) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                delegate.save(key, value);
                return;                                   // 成功立即返回
            } catch (RuntimeException e) {
                last = e;
                System.out.println("[RETRY] 第 " + attempt + " 次写入失败: " + e.getMessage());
            }
        }
        throw new IllegalStateException("重试 " + maxRetries + " 次仍失败, key=" + key, last);
    }

    @Override
    public String read(String key) {
        return delegate.read(key);                        // 读操作不做重试，按需加强
    }
}
