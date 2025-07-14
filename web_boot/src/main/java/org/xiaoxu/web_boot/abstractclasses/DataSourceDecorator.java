package org.xiaoxu.web_boot.abstractclasses;

import org.xiaoxu.web_boot.service.DataSource;

/**
 * @className: DataSourceDecorator
 * @author: xiaoxu
 * @date: 2025/7/13 10:48
 * @Version: 1.0
 * @description: 装饰器抽象类 Decorator
 * 装饰器抽象类，实现了抽象构建Component接口，同时持有一个抽象构建Component类型的引用。
 * 装饰器类通常会在具体装饰器类中实现具体的装饰逻辑。
 * 这里的装饰器类是一个抽象类，它的具体实现类将在后面的具体装饰器类中进行。

 */
public abstract  class DataSourceDecorator implements DataSource {

    protected DataSource wrappee;

    public DataSourceDecorator(DataSource source) {
        this.wrappee = source;
    }

    @Override
    public void writeData(String data) {
        wrappee.writeData(data);
    }

    @Override
    public String readData() {
        return wrappee.readData();
    }
}
