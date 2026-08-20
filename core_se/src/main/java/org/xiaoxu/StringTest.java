package org.xiaoxu;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @className: StringTest
 * @author: xiaoxu
 * @date: 2025/8/12 20:28
 * @Version: 1.0
 * @description:
 */
public class StringTest {

    private static final Logger log = LoggerFactory.getLogger(StringTest.class);

    public static void main(String[] args) {

//        ///
//        String str = "a,b,c";
//        String result = str.replaceAll(",","&");
//        System.out.println(result);
//        /////如果展示集合:
//        List<String> res = List.of(String.join("&", str.split(",")));
//        System.out.println(res);

        Integer a = -128;
        Integer b = -128;
        System.out.println(a == b);

        Integer c = 128;
        Integer d = 128;
        System.out.println(c == d);




       /* String str = "ABc";
        String st2 = "ABc";

        String intern = new String("BAC").intern();
        System.out.println(intern);
        System.out.println(str.hashCode());
        System.out.println(st2.hashCode());

        System.out.println(str.equals(st2));

        if (StringUtils.equalsIgnoreCase(str, st2)) {
            log.info("已经忽略了大小写了 ");
        }else {
            log.info("没有忽略大小写");
        }*/

    }
}
