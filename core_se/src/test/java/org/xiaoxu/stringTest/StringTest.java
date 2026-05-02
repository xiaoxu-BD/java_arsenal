package org.xiaoxu.stringTest;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


/**
 * @className: StringTest
 * @author: xiaoxu
 * @date: 2025/7/26 9:43
 * @Version: 1.0
 * @description:
 */
public class StringTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(StringTest.class);


//    StandardCharsets 标准的字符变量编码

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
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        log.info("{}",bytes);
        //字节到 字面量 解码
        String result = new String(bytes, StandardCharsets.UTF_8);
        log.info("{}",result);

    }

    public void testDeSugar(){
        List<String> list = Arrays.asList("apple", "pear", "banana", "orange");
        }



     public void testAssert() {
        int a =1 ;
        int b = 2;
        assert a == b;
     }


     public void testStringJSON(){
        String json = "{\"form\":{\"inline\":false,\"hideRequiredAsterisk\":false,\"labelPosition\":\"right\",\"size\":\"default\",\"labelWidth\":\"100px\"},\"resetBtn\":{\"show\":false,\"innerText\":\"重置\"},\"submitBtn\":{\"show\":true,\"innerText\":\"提交\"},\"formName\":\"新表单\"}";

         Object parse = JSON.parse(json);
         String prettyJson = JSON.toJSONString(parse, JSONWriter.Feature.PrettyFormat);
         System.out.println(prettyJson);
     }




     public void testStringEqualsIgnoreCase(){
        String str = "8ui0";
        String str2 = "8Ui0";
         boolean result = StringUtils.equalsIgnoreCase(str, str2);
         Assert.isTrue(result,()-> new RuntimeException("is not true"));
     }



     public void testFloor(){
         System.out.println(Math.floor(10.9));
         System.out.println(Math.floor(10.1));
     }
    }



