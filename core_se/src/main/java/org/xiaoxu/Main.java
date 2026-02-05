package org.xiaoxu;

import com.alibaba.fastjson2.JSON;
import org.xiaoxu.entity.WeekORM;
import org.xiaoxu.enums.Week;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @className: Main
 * @author: xiaoxu
 * @date: 2026/1/9 23:03
 * @Version: 1.0
 * @description:
 */
public class Main {


    public static void main(String[] args) {

     String jons =   """
             {
               "change": "1",
               "action": "decrease",
               "from": "94",
               "timestamp": 1765550883698,
               "by": "DECREASE_1019994914600676761600001",
               "to": 93
             }
             
                """;

        System.out.println(jons);

        int x = Integer.MAX_VALUE;
        System.out.println(x + 1); // -2147483648（发生溢出）

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        executorService.execute(() -> {
            System.out.println("hello world");
        });
        executorService.shutdown();


        WeekORM weekORM = new WeekORM();
        weekORM.setNums(Week.FRIDAY);



        Boolean isTrue = null;
        String s = null;
            // && 原来是左边先执行啊,然后再右边执行,左边为false 右边根本不执行
        if (s.length() > 0 && s != null) {
            System.out.println("A");
        } else {
            System.out.println("B");
        }

        // Boolean对象是会自动使用: booleanValue()方法进行转换的
        if (isTrue){
            System.out.println("C");
        }


    }
}
