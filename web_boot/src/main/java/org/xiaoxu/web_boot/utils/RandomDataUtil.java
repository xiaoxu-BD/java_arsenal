package org.xiaoxu.web_boot.utils;

import java.util.Random;

public class RandomDataUtil {
    private static final Random random = new Random();

    // 随机姓名，中文 + 数字后缀
    public static String randomName() {
        int len = 2 + random.nextInt(2); // 2~3个字
        StringBuilder sb = new StringBuilder("客户");
        for (int i = 0; i < len; i++) {
            char c = (char) (0x4e00 + random.nextInt(0x9fa5 - 0x4e00)); // 中文汉字随机
            sb.append(c);
        }
        sb.append(random.nextInt(1000)); // 加个数字后缀
        return sb.toString();
    }

    // 随机手机号
    public static String randomPhone() {
        StringBuilder sb = new StringBuilder("1");
        sb.append(3 + random.nextInt(7)); // 第二位 3~9
        for (int i = 0; i < 9; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    // 随机邮箱
    public static String randomEmail() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        int len = 6 + random.nextInt(5); // 6~10位
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        sb.append("@example.com");
        return sb.toString();
    }
}
