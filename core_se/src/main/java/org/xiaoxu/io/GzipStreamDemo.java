package org.xiaoxu.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * L4 练习: 装饰器的实战 —— 压缩流。
 * FileOutputStream → GZIPOutputStream, 管口没变(write 方法一样), 但流出去的字节已经是压缩过的。
 * 底座换了"水", 管子用法零改动, 这就是装饰器模式的价值。
 */
public class GzipStreamDemo {

    public static void main(String[] args) throws IOException {
        Path dir = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "io-practice"));

        // 重复度高的文本压缩率极高, 效果才震撼
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append("第 ").append(i).append(" 行: 同样的内容重复两千遍, 压缩最爱这种\n");
        }
        byte[] raw = sb.toString().getBytes(StandardCharsets.UTF_8);

        // 写原始文件
        Path txt = dir.resolve("04-原始.txt");
        Files.write(txt, raw);

        // 写压缩文件: 只是给出口管子多套了一层 GZIP
        Path gz = dir.resolve("04-压缩.gz");
        try (GZIPOutputStream out = new GZIPOutputStream(
                new FileOutputStream(gz.toFile()))) {
            out.write(raw);
        }

        // 读回验证: 入口管子套一层 GZIPInputStream, 读法完全不变
        String back;
        try (GZIPInputStream in = new GZIPInputStream(
                new FileInputStream(gz.toFile()));
             ByteArrayOutputStream mem = new ByteArrayOutputStream()) {
            in.transferTo(mem);
            back = mem.toString(StandardCharsets.UTF_8);
        }

        System.out.println("✅ 压缩前 " + raw.length / 1024 + " KB -> 压缩后 "
                + Files.size(gz) / 1024 + " KB");
        System.out.println("✅ 解压读回内容一致: " + (back.equals(sb.toString())));
    }
}
