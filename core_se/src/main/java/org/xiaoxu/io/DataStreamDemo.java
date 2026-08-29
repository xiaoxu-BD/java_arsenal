package org.xiaoxu.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * L5 练习: 基本类型读写 —— DataInputStream/DataOutputStream。
 * 字节流只认字节, 这对管子加了"按 Java 基本类型切块"的能力: writeInt 固定写 4 字节(大端序)。
 * 写的顺序 = 读的顺序, 这份"约定"就是自定义二进制协议的最小雏形,
 * TCP 粘包/拆包、RPC 框架的编解码, 根都在这里。
 */
public class DataStreamDemo {

    public static void main(String[] args) throws IOException {
        Path dir = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "io-practice"));
        Path file = dir.resolve("05-二进制协议.bin");

        // 发送方: 按约定顺序写 —— [int 长度][long id][double 金额][UTF 字符串备注]
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file.toFile())))) {
            out.writeInt(3);                      // 表示后面跟 3 条记录? 这里演示用, 只写了一条
            out.writeLong(10001L);                // 订单号, 固定 8 字节
            out.writeDouble(199.99);              // 金额, 固定 8 字节
            out.writeUTF("第一期打款");            // UTF 字符串, 自带长度前缀
        }

        // 接收方: 严格按同样顺序读, 错一个顺序整个字节流就"错位"了
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file.toFile())))) {
            int count = in.readInt();
            long orderId = in.readLong();
            double amount = in.readDouble();
            String remark = in.readUTF();

            System.out.println("✅ 按协议读回: count=" + count + ", orderId=" + orderId
                    + ", amount=" + amount + ", remark=" + remark);
        }
        System.out.println("✅ 文件大小 " + Files.size(file) + " 字节 (4+8+8+UTF前缀+汉字字节, 可以手算验证)");
    }
}
