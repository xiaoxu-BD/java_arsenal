package org.xiaoxu.io;

import cn.hutool.core.util.IdUtil;
import com.google.common.base.Strings;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.junit.Assert;
import org.junit.Test;
import org.xiaoxu.entity.Student;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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
public class FileTest {

    @Test
    public void testIO(){
        File file = new File("E:\\dev-gradual");

//        log.info("{}",file.exists());
        String[] list = file.list();
        Assert.assertNotNull(list);
        for (String fileName : list) {
            if (fileName.endsWith(".doc")|| fileName.endsWith(".docx")){
                log.info("{}",fileName);
            }
            if (fileName.endsWith(".txt")){
                log.info("{}",fileName);
            }
        }
    }

    @Test
    public void testFileStream(){
//        try {
//            InputStream fis = new FileInputStream("E:\\dev-gradual\\secret.txt");
//            int data;
//            while ((data = fis.read()) != -1) {
//                log.info("data:{}",(char)data);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        //通常使用: BufferedInputStream
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream("E:\\dev-gradual\\secret.txt"));
            String strings = new String(bufferedInputStream.readAllBytes());
            String resultWithOutBlankSpace = strings.trim();
            log.info("result:{}",resultWithOutBlankSpace);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
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
        @Test
        public void testInput() throws IOException {
            File file = new File("E:\\dev-gradual\\secret.txt");
            FileReader fileReader = new FileReader(file);

            int data;
            while ((data = fileReader.read()) != -1) {
//          System.out.print((char) data);
                log.info("data:{}",(char)data);
            }

            fileReader.close();

            }
            @Test
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

            public void testObjectsAPI(){
                Student student = new Student();
                student.setId("1");
                Student message = Objects.requireNonNull(student, "student 不能为空");
                log.info("{}",message);
            }


            public void testCommonsIO() throws IOException {
                File file = new File("E:\\dev-gradual\\毕业源代码\\node.txt");
                String content = FileUtils.readFileToString(file, "utf-8");
//                FileUtils.writeStringToFile(file, "测试FileUtils", "utf-8",false);
                log.info("{}",content);

                String extension = FilenameUtils.getExtension("node.txt");
                log.info(" 后缀名为： {}",extension);
            }
    @Test
    public void testCommonsIO2() throws IOException {
        File file = new File("output.txt");
        String content = "你好，世界";

        // 写入文件，指定 UTF-8 编码
        FileUtils.writeStringToFile(file, content, "UTF-8", false);
        log.info(" 写入完成 {}",content);
    }




            public void testRandomFileName(){
                File file = new File("E:\\dev-gradual\\毕业源代码\\node.txt");
                String fileName = file.getName();
                String randRomName = this.getFileName(fileName);
                log.info("{}",randRomName);
            }

            public void testObjectStream(){

            String filePath = "student.txt";
//            serializeObject(filePath);


                deserializeObject(filePath);

            }


    public static void serializeObject(String filePath) {
        // 创建一个 Person 对象
        Student student = new Student();
        student.setId("1");
        student.setName("xtq");
        student.setAge(23);

        try (FileOutputStream fileOut = new FileOutputStream(filePath);
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {

            // 将对象写入文件
            objectOut.writeObject(student);
            System.out.println("序列化完成，对象已写入文件: " + filePath);
            System.out.println("序列化的对象: " + student);

        } catch (IOException e) {
          log.error("序列化对象时发生错误: " + e.getMessage());
        }
    }

    // 反序列化方法
    public static void deserializeObject(String filePath) {
        try (FileInputStream fileIn = new FileInputStream(filePath);
             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {

            // 从文件读取对象
            Student person = (Student) objectIn.readObject();
            System.out.println("反序列化完成，对象已从文件读取");
            System.out.println("反序列化的对象: " + person);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


            public String getFileName(String fileName){
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String datePart = dateFormat.format(new Date());
                long prefixName = IdUtil.getSnowflakeNextId();
                String extension = FilenameUtils.getExtension(fileName);
                return String.format("%s/%s.%s", datePart, prefixName, extension);
            }

    public void testSerializable () throws Exception {
        A a1 = new A(123, "abc");
        String objectFile = "serializable.txt";

//        - 创建一个对象输出流，它内部包装了 `FileOutputStream`。
//        - `FileOutputStream` 打开（或创建）指定的文件用于写入字节；
//        - `ObjectOutputStream` 用于将对象以序列化形式写入字节流
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(objectFile));
        objectOutputStream.writeObject(a1);
        objectOutputStream.close();

        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(objectFile));
        A a2 = (A) objectInputStream.readObject();
        objectInputStream.close();
        System.out.println(a2);
    }

    private static class A implements Serializable {
        private int x;
        private String y;

        A(int x, String y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "x = " + x + "  " + "y = " + y;
        }
    }

}



