package org.xiaoxu;


public class StringDemo {


    public static void main(String[] args) {

        demo01();


    }



    public static void demo01(){
        String input = "\u3000 ORD-1001 ";   // \u3000 是全角空格，中文输入法很常见

        input.trim();    // "\u3000 ORD-1001"  -> trim() 去不掉全角空格
        String strip = input.strip();// "ORD-1001"         -> strip() 能识别 Unicode 空白（Java 11+）
        input.stripLeading();
        System.out.println(strip);
    }


    public static void equalsD(){
/*        // 正确：常量写在前面
        if ("PAID".equals(order.getStatus())) { ... }

// 正确：两边都可能为 null

        if (Objects.equals(a, b)) { ... }

// 忽略大小写，比如币种 "usd" 和 "USD"
        if ("USD".equalsIgnoreCase(currency)) { ... }*/

        String a = null ;
        String b = null;

        // 正确：两边都可能为 null 使用Objects

    }
}
