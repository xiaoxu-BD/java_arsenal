package org.xiaoxu.collection;

import org.xiaoxu.entity.Person;

import java.util.*;

/**
 * @className: CollectionMain
 * @author: xiaoxu
 * @date: 2025/9/5 11:24
 * @Version: 1.0
 * @description:
 */
public class CollectionMain {

    public static void main(String[] args) {

        List<Person> people = Arrays.asList(new Person("xx1", 18), new Person("xx2", 28),new Person("xx1", 29),new Person("xx4", 30));
        //不可变的集合不能排序
        Collections.sort(people,Comparator.comparing(Person::getName).thenComparingInt(Person::getAge));
//        System.out.println(people);
         List<Person> personList = people.stream().sorted((CollectionMain::compare)).toList();
         people.iterator();
          personList.forEach(System.out::println);

//        people.stream().map(Person::getName).toList().forEach(System.out::println);
    }

    private static int compare(Person o1, Person o2) {
        int result = o1.getName().compareTo(o2.getName());
        if (result == 0) {
            result = o1.getAge() - o2.getAge();
        }
        return result;
    }
}
