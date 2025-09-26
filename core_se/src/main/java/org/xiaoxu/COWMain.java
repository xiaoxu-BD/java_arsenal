package org.xiaoxu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @className: COWMain
 * @author: xiaoxu
 * @date: 2025/9/9 10:34
 * @Version: 1.0
 * @description:
 */
public class COWMain {
    volatile static String a = "aaaaa";
    private static final Logger log = LoggerFactory.getLogger(COWMain.class);

    public static void main(String[] args) {
        CopyOnWriteArrayList<Object> stringList = new CopyOnWriteArrayList<>();
        stringList.add("hello");
        stringList.add("holy");
        stringList.add("shit");

        new Thread(()->{
            for (Object s : stringList) {
                System.out.println(s);

            }
            log.info("数组长度为:{}",stringList.size());
        }).start();


        new Thread(()->{
            stringList.add("world");
            stringList.add(a);
            System.out.println("添加了 world");
        }).start();


        stringList.forEach(System.out::println);

        log.info("数组长度为:{}",stringList.size());

    }
}
