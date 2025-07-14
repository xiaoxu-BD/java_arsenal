package org.xiaoxu.web_boot.strategy.decorator;

import org.xiaoxu.web_boot.abstractclasses.DataSourceDecorator;
import org.xiaoxu.web_boot.service.DataSource;

public class CompressionDecorator extends DataSourceDecorator {
    public CompressionDecorator(DataSource source) {
        super(source);
    }

    @Override
    public void writeData(String data) {
        String compressed = "压缩(" + data + ")";
        super.writeData(compressed);
    }

    @Override
    public String readData() {
        String data = super.readData();
        return "解压(" + data + ")";
    }
}
