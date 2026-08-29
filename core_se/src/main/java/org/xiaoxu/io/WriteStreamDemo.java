package org.xiaoxu.io;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * L1 练习: 写出流。和读取完全对称 —— 读是 read(), 写是 write(), 写完记得 flush/close。
 * 文件统一写到系统临时目录 io-practice 下, 不弄脏仓库。
 */
public class WriteStreamDemo {

    public static void main(String[] args) throws IOException {
        Path dir = Files.createDirectories(Path.of("io-practice"));

        rawBytes(dir.resolve("01-裸字节.txt"));
        bufferedLines(dir.resolve("02-按行写.txt"));
        appendMode(dir.resolve("02-按行写.txt"));

        System.out.println("三个演示跑完, 去目录里打开文件看内容: " + dir);
    }

    /** 演示1: 最原始的字节写入。OutputStream 只认字节, 字符串要自己先转成字节数组 */
    static void rawBytes(Path file) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file.toFile())) {
            out.write("第一行: 我是一串 UTF-8 字节\n".getBytes(StandardCharsets.UTF_8));
            out.write("第二行: 裸流没有缓冲, write 一个字节就搬一次\n".getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("✅ 裸字节写入完成: " + file.getFileName());
    }

    /** 演示2: 洋葱模型写字符 —— 底座 FileOutputStream, 中间加解码器(字符→字节), 最外层加蓄水池(按行) */
    static void bufferedLines(Path file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file.toFile()), StandardCharsets.UTF_8))) {
            writer.write("第一行: 按行写才是文本的正道");
            writer.newLine(); // newLine 比 手写 \n 更稳, 它跟平台换行符走
            writer.write("第二行: BufferedWriter 攒一坨才真正落盘");
            writer.newLine();
            // close 时会自动 flush; 如果不 close 想立刻生效, 手动 writer.flush()
        }
        System.out.println("✅ 按行写入完成: " + file.getFileName());
    }

    /** 演示3: 追加模式 —— new FileOutputStream(file, true), 第二个参数 true = 接着写而不是覆盖 */
    static void appendMode(Path file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file.toFile(), true), StandardCharsets.UTF_8))) {
            writer.write("第三行: 追加模式写进来的, 前两行还在");
            writer.newLine();
        }
        System.out.println("✅ 追加写入完成, 文件应该是 3 行: " + file.getFileName());
    }
}
