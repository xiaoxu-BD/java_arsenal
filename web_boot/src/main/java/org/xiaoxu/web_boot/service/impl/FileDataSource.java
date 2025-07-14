package org.xiaoxu.web_boot.service.impl;

import org.xiaoxu.web_boot.service.DataSource;

/**
 * @className: FileDataSource
 * @author: xiaoxu
 * @date: 2025/7/13 10:45
 * @Version: 1.0
 * @description: 具体构件：ConcreteComponent
 * 实现了抽象构建Component接口的具体实现类，提供了具体的业务逻辑。
 * 这里的具体实现是针对文件数据源的读写操作。
 */
public class FileDataSource implements DataSource {

    private final String filename;

    public FileDataSource(String filename) {
        this.filename = filename;
    }
    @Override
    public void writeData(String data) {
        System.out.println("写入数据到文件：" + filename + ", 内容：" + data);
    }

    @Override
    public String readData() {
        System.out.println("从文件中读取数据：" + filename);
        return "原始数据";
    }
    }

