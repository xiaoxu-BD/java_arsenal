package org.xiaoxu.web_boot.strategy.decorator;

import org.xiaoxu.web_boot.abstractclasses.DataSourceDecorator;
import org.xiaoxu.web_boot.service.DataSource;

/**
 * @className: EncryptionDecorator
 * @author: xiaoxu
 * @date: 2025/7/13 10:51
 * @Version: 1.0
 * @description: 具体装饰器：ConcreteDecorator
 * 具体装饰器类，继承了装饰器抽象类，实现了具体的装饰逻辑。
 * 这里的具体装饰器类是一个加密装饰器，它的具体实现是对数据进行加密操作。
 * 具体装饰器类的实现方式是在具体装饰器类中实现具体的装饰逻辑。
 */
public class EncryptionDecorator extends DataSourceDecorator {
    public EncryptionDecorator(DataSource source) {
        super(source);
    }
    @Override
    public void writeData(String data) {
        String encrypted = "加密(" + data + ")";
        super.writeData(encrypted);
    }

    @Override
    public String readData() {
        String data = super.readData();
        return "解密(" + data + ")";
    }


}
