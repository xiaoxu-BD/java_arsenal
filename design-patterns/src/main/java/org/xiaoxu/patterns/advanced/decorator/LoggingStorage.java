package org.xiaoxu.patterns.advanced.decorator;

/** 装饰器：读写前后打日志（排障用） */
public class LoggingStorage extends StorageDecorator {

    public LoggingStorage(Storage delegate) {
        super(delegate);
    }

    @Override
    public void save(String key, String value) {
        System.out.println("[LOG] save key=" + key + " value=" + value);
        delegate.save(key, value);
    }

    @Override
    public String read(String key) {
        String value = delegate.read(key);
        System.out.println("[LOG] read key=" + key + " -> " + value);
        return value;
    }
}
