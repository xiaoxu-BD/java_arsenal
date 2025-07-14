package org.xiaoxu.web_boot.service;

// 抽象构建Component
public interface DataSource {
    void writeData(String data);
    String readData();
}