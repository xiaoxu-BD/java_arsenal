package org.xiaoxu.io;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @className: FileTest
 * @author: xiaoxu
 * @date: 2025/5/22 21:19
 * @Version: 1.0
 * @description: IO 都在站在内存的角度 去读取和写入的
 *
 */
@Slf4j
public class FileTest  extends TestCase {


    public void testIO(){
        File file = new File("E:\\dev-gradual\\毕业源代码\\附件");

//        log.info("{}",file.exists());
        String[] list = file.list();
        assert list != null;
        for (String fileName : list) {
            if (fileName.endsWith(".doc")|| fileName.endsWith(".docx")){
                log.info("{}",fileName);
            }
        }
    }


    public void testIO2() {
        File file = new File("E:\\dev-gradual\\毕业源代码\\附件");
        Set<String> collect = Arrays.stream(Objects.requireNonNull(file.list((dir, name) -> name.endsWith(".docx")))).collect(Collectors.toSet());

        for (String fileName : collect) {
            log.info("{}", fileName);
        }

    }


        /**
         * 输入流 input 外部资源东西到内存中
         * 输出流 😀output 内存中东西到外部资源
         * 字符流: Reader Writer 一字符为单位读取数据:　专门处理处理文本文件，不能用于处理图片，音频，视频文件
         */
        public void testInput() throws IOException {
            File file = new File("E:\\dev-gradual\\毕业源代码\\node.txt");
            FileReader fileReader = new FileReader(file);

            int data;
            while ((data = fileReader.read()) != -1) {
          System.out.print((char) data);
//                log.info("data:{}",(char)data);
            }

            fileReader.close();

            }
            public void testOutput() throws IOException {
            File file = new File("E:\\dev-gradual\\毕业源代码\\node.txt");
                FileWriter fileWriter = new FileWriter(file);

                // 直接覆盖值
                char[] chars = "xtq".toCharArray();
                char[] chars2 = {'a','b','c','d'};
                fileWriter.write(chars);
                fileWriter.write(chars2);
                fileWriter.close();

                FileReader fileReader = new FileReader(file);
                int data;
                while((data = fileReader.read())!= -1){
                    System.out.print((char)data);
                }
            }
        }



