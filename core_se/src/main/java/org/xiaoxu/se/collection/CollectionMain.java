package org.xiaoxu.se.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class CollectionMain {
    public static void main(String[] args) {


        /*
        * CopyOnWrite 不会触发ConcurrentModificationException；
        * 如果普通的集合想要删除的话就会报错，必须使用迭代器iterator
        * */
//        copyOnWriter();


        ArrayList<String> strings = new ArrayList<>();
        strings.add("111100");
        strings.add("111111");
        strings.add("11113");
        strings.add("11114");
        strings.add("11112");
        strings.removeIf( e -> e.equals("111100"));
        List<String> list = strings.stream().filter(Objects::nonNull).filter(e ->  !e.equals("11114")).toList();
        list.forEach(System.out::println);
    }

    private static void copyOnWriter() {
        CopyOnWriteArrayList<String> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        copyOnWriteArrayList.add("2");
        copyOnWriteArrayList.add("22");
        copyOnWriteArrayList.add("23");
        copyOnWriteArrayList.add("24");
        for (String copyOnWriter : copyOnWriteArrayList) {
            if (copyOnWriter.equals("2")){
                copyOnWriteArrayList.remove(copyOnWriter);
            }
        }
        System.out.println(copyOnWriteArrayList);
    }
}
