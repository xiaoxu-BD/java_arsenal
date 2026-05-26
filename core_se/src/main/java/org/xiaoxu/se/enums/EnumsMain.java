package org.xiaoxu.se.enums;

public class EnumsMain {

    public static void main(String[] args) {

//        enumExampleTest();


        Seasons summer = Seasons.valueOf("SUMMER");
        System.out.println(summer.name());
//        调用了toString的方法
        System.out.println(summer);
    }

    private static void enumExampleTest() {
        // 1. 基本用法
        System.out.println("=== 1. 基本用法 ===");
        Seasons spring = Seasons.SPRING;
        System.out.println(spring);
        System.out.println("desc: " + spring.getDesc());
        System.out.println("ordinal: " + spring.ordinal());
        System.out.println("name: " + spring.name());

        // 2. switch
        System.out.println("\n=== 2. switch ===");
        switch (spring) {
            case SPRING -> System.out.println("春暖花开");
            case SUMMER -> System.out.println("烈日炎炎");
        }

        // 3. 遍历
        System.out.println("\n=== 3. 遍历 ===");
        for (Seasons s : Seasons.values()) {
            System.out.println(s + " → " + s.getDesc());
        }

        // 4. 字符串解析
        System.out.println("\n=== 4. valueOf ===");
        Seasons parsed = Seasons.valueOf("SPRING");
        System.out.println("valueOf(\"SPRING\") = " + parsed);
        try {
            Seasons.valueOf("HELLO");
        } catch (IllegalArgumentException e) {
            System.out.println("valueOf(\"HELLO\") → " + e.getMessage());
        }

        // 5. 比较用 ==（枚举是单例）
        System.out.println("\n=== 5. == 比较 ===");
        System.out.println(Seasons.SPRING == Seasons.SPRING);  // true

        // 6. 抽象方法枚举
        System.out.println("\n=== 6. Payment 抽象方法 ===");
        System.out.println("PayPal 100元手续费: " + Payment.PAYPAL.getFee(100));
        System.out.println("WeChat 100元手续费: " + Payment.WECHAT.getFee(100));
        System.out.println("Bank   100元手续费: " + Payment.BANK.getFee(100));

        // 7. 实现接口的枚举
        System.out.println("\n=== 7. Color 实现接口 ===");
        for (Color c : Color.values()) {
            c.printInfo();
        }
    }
}
