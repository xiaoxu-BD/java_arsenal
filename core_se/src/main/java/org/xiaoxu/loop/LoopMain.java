package org.xiaoxu.loop;

import org.apache.commons.lang3.time.StopWatch;

import java.util.Scanner;

/**
 * @className: LoopMain
 * @author: xiaoxu
 * @date: 2025/10/19 20:52
 * @Version: 1.0
 * @description:
 */
public class LoopMain {


    public static void main(String[] args) {

        //expression 有限 byte short int char
        //value 的值要确定 
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        switch (n) {

           case 1,2,3,4,5 -> System.out.println("workDay");
           case 6,7 -> System.out.println("weekend");
            default -> System.out.println("not in  week");
        }
    }
}
