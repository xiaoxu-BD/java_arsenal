package org.xiaoxu.collection;

import org.xiaoxu.entity.Person;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @className: CollectionMain
 * @author: xiaoxu
 * @date: 2025/9/5 11:24
 * @Version: 1.0
 * @description:
 */
public class CollectionMain {

    public static void main(String[] args) throws Exception {

//        extracted();
//        test();
        pecsDemo();
    }

    private static void extracted() {
        List<Person> people = Arrays.asList(new Person("xx1", 18), new Person("xx2", 28),new Person("xx1", 29),new Person("xx4", 30));
        //不可变的集合不能排序
        Collections.sort(people,Comparator.comparing(Person::getName).thenComparingInt(Person::getAge));
//        System.out.println(people);
        List<Person> personList = people.stream().sorted((CollectionMain::compare)).toList();
        people.iterator();
        personList.forEach(System.out::println);
    }

    private static int compare(Person o1, Person o2) {
        int result = o1.getName().compareTo(o2.getName());
        if (result == 0) {
            result = o1.getAge() - o2.getAge();
        }
        return result;
    }




    public static void test() throws Exception {
        ArrayList<Integer> list = new ArrayList<Integer>();
/*        list.getClass().newInstance();
        list.getClass().getDeclaredConstructor().newInstance();*/
        Method method = list.getClass().getMethod("add", Object.class);
        method.setAccessible(true);
        method.invoke(list, "Java反射机制实例");
        System.out.println(list.get(0));
    }

    // ========== PECS 泛型上下界演示 ==========
    //PECS Product extend    Consumer super

    // extends 上界：只读不写（Consumer / 消费者视角，从里面取数据用）
    public static double sumOfList(List<? extends Number> list) {
        double sum = 0;
        for (Number n : list) {
            sum += n.doubleValue();
        }
        // list.add(1);    // 编译报错！无法确认实际类型是 Integer 还是 Double
        return sum;
    }

    // super 下界：只写难读（Producer / 生产者视角，往里面塞数据用）
    public static void addIntegers(List<? super Integer> list) {
        list.add(1);
        list.add(2);
        list.add(3);
        // Integer x = list.get(0);  // 编译报错！取出来只能当 Object
        Object x = list.get(0);
        System.out.println("  取出元素类型: " + x.getClass().getSimpleName() + " = " + x);
    }

    public static void pecsDemo() {
        System.out.println("===== extends 上界：当 Number 的子类集合来读 =====");

        List<Integer> ints = Arrays.asList(1, 2, 3);
        List<Double> doubles = Arrays.asList(1.1, 2.2, 3.3);
        List<Float> floats = Arrays.asList(1.5f, 2.5f, 3.5f);

        System.out.println("List<Integer> sum = " + sumOfList(ints));
        System.out.println("List<Double>  sum = " + sumOfList(doubles));
        System.out.println("List<Float>   sum = " + sumOfList(floats));

        System.out.println("\n===== super 下界：往 Integer 的父类集合里写 =====");

        List<Number> numbers = new ArrayList<>();
        System.out.println("往 List<Number> 里 add Integer:");
        addIntegers(numbers);

        List<Object> objects = new ArrayList<>();
        System.out.println("往 List<Object> 里 add Integer:");
        addIntegers(objects);

        System.out.println("\n===== 为什么不安全？ =====");
        System.out.println("extends 不让写：假如实际传的是 List<Integer>，你塞个 Double 进去 → ClassCastException");
        System.out.println("super 不让读成具体类型：假如实际是 List<Number>，取出来可能是 Double，不是 Integer");
    }
}
