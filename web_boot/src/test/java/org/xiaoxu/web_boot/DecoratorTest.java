package org.xiaoxu.web_boot;

import org.xiaoxu.web_boot.service.DataSource;
import org.xiaoxu.web_boot.service.impl.FileDataSource;
import org.xiaoxu.web_boot.strategy.decorator.CompressionDecorator;
import org.xiaoxu.web_boot.strategy.decorator.EncryptionDecorator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @className: DecoratorTest
 * @author: xiaoxu
 * @date: 2025/7/13 10:56
 * @Version: 1.0
 * @description:
 */
public class DecoratorTest {
    public static void main(String[] args) throws IOException {
//        DataSource file = new FileDataSource("data.txt");
//
//        // 包装成压缩+加密
//        DataSource decorated = new CompressionDecorator(new EncryptionDecorator(file));
//
//        decorated.writeData("重要数据");
//        String result = decorated.readData();
//        System.out.println("最终读取数据：" + result);

        //读取一大块字符 ➜ 缓存 ➜ 按行解析 ➜ 返回行 ➜ 控制台输出。
        String filePath = "C:\\Users\\24813\\Desktop\\test.json";
        //作用： 打开磁盘上的文本文件，读取字符数据（不是二进制）
        FileReader fileReader = new FileReader(filePath);

        // 在 Reader 基础上增加了「缓冲 + 一次读取一整行的能力」
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }

    }
}
