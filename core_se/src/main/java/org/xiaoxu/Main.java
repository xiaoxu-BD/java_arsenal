package org.xiaoxu;

import com.alibaba.fastjson2.JSON;

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
    }
}
