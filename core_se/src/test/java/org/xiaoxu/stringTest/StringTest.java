package org.xiaoxu.stringTest;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static cn.hutool.core.lang.Console.print;

/**
 * @className: StringTest
 * @author: xiaoxu
 * @date: 2025/7/26 9:43
 * @Version: 1.0
 * @description:
 */
public class StringTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(StringTest.class);

    public void testString(){
        String s = "abcd";
        String s3 = "f";
        // + 运算符会调用StringBuilder的append方法，append方法会调用String的concat方法，concat方法会new一个String对象
        String s4 =  s + s3;
        System.out.println(s4);
    }
    public void testEncode() throws UnsupportedEncodingException {
        String s = "你好";
        // 字面量 到 字节 称为 编码
        byte[] bytes = s.getBytes("UTF-8");
        log.info("{}",bytes);
        //字节到 字面量 解码
        String result = new String(bytes,"UTF-8");
        log.info("{}",result);

    }

    public void testDeSugar(){
        List<String> list = Arrays.asList("apple", "pear", "banana", "orange");
        print(String.valueOf(list));
        }



     public void testAssert() {
        int a =1 ;
        int b = 2;
        assert a == b;
     }
    }



