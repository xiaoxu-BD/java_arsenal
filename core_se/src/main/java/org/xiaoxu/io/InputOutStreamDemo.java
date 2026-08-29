package org.xiaoxu.io;

import java.io.*;

public class InputOutStreamDemo {
    public static void main(String[] args) throws IOException {
        //字节
/*        FileInputStream fileInputStream = new FileInputStream("E:\\test.txt");
        byte[]  bytes = new byte[1024];
        int read = fileInputStream.read(bytes);
        String content = new String(bytes,0,read);
        System.out.println(content);*/

        //文本文件使用字符！！ 有编码器
/*        FileReader fr = new FileReader("E:\\test.txt");
        char[] chars = new char[1024];
        int read = fr.read(chars);
        String content = new String(chars, 0, read); // 只取实际读取的部分
        System.out.println(content); // ✅ 打印完整内容  // ✅ 打印字符数组内容*/


        FileInputStream bigDataInputStream = new FileInputStream("C:\\Users\\tingq\\Downloads\\用户导出_2026-08-29 (1).xlsx");

        BufferedInputStream bufferedIS = new BufferedInputStream(bigDataInputStream);
//        bufferedIS.

                //bufferedOutputStream
    }
}
