package org.xiaoxu;

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

    public static void main(String[] args) {

        ///
        String str = "a,b,c";
        String result = str.replaceAll(",","&");
        System.out.println(result);
        /////如果展示集合:
        List<String> res = List.of(String.join("&", str.split(",")));
        System.out.println(res);

    }
}
