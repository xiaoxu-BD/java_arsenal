package org.xiaoxu.patterns.advanced.decorator;

/**
 * 装饰器基类（高级版 Decorator）：包着一个 Storage，自己也是 Storage
 *
 * <p>所有装饰器继承它，只需专注写"自己要加强的那一段"，
 * 不用每个装饰器都重复写委托代码。
 */
public abstract class StorageDecorator implements Storage {

    /** 被装饰对象：可能是裸存储，也可能是别的装饰器——套娃由此而来 */
    protected final Storage delegate;

    protected StorageDecorator(Storage delegate) {
        this.delegate = delegate;
    }
}
