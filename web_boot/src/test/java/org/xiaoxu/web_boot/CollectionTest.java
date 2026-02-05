package org.xiaoxu.web_boot;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.xiaoxu.web_boot.mapper.convert.UserConvertor;
import org.xiaoxu.web_boot.mapper.convert.UserVOConvert;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @className: CollectionTest
 * @author: xiaoxu
 * @date: 2025/9/9 22:03
 * @Version: 1.0
 * @description:
 */

public class CollectionTest {
    private static final Logger log = LoggerFactory.getLogger(CollectionTest.class);
    private HashMap<String,Integer> studentScores;

    private List<String> names;
    private List<Integer> numbers;
    private Map<String, Integer> productPrices;
    private List<Employee> employees;

    static class Employee {
        String name;
        int age;
        int salary;


        public Employee(String name, int age, int salary) {
            this.name = name;
            this.age = age;
            this.salary = salary;
        }
        public String getName() {
            return name;
        }
        public int getSalary(){
            return salary;
        }
        public int getAge(){
            return age;
        }

        @Override
        public String toString() {
            return name + " (Age: " + age + ", Salary: " + salary + ")";
        }
    }
    @BeforeEach
    public void setUp(){
            studentScores = new HashMap<>();
            studentScores.put("张三", 85);
            studentScores.put("李四", 92);
            studentScores.put("王五", 78);
            studentScores.put("赵六", 96);
            studentScores.put("钱七", 88);
            studentScores.put("孙八", 75);
            studentScores.put("周九", 91);
            studentScores.put("吴十", 82);


        // 1. 字符串列表
        names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve", "Anna", "Alex");

        // 2. 数字列表
        numbers = Arrays.asList(10, 5, 8, 20, 3, 15, 7, 12, 9, 18);

        // 3. 商品价格 Map
        productPrices = new HashMap<>();
        productPrices.put("Apple", 50);
        productPrices.put("Banana", 30);
        productPrices.put("Orange", 40);
        productPrices.put("Grapes", 80);
        productPrices.put("Mango", 60);
        productPrices.put("Pineapple", 70);

        // 4. 员工列表（自定义类）
        employees = Arrays.asList(
                new Employee("Alice", 25, 50000),
                new Employee("Bob", 30, 60000),
                new Employee("Charlie", 22, 45000),
                new Employee("David", 35, 80000),
                new Employee("Eve", 28, 55000),
                new Employee("Frank", 40, 90000)
        );
        }

//    // 我的问题1：请使用lambda表达式找出所有成绩大于90分的学生及其成绩
//        System.out.println("\n问题1：找出成绩大于90分的学生");
        @Test
        public void testOverAll90(){
        studentScores.entrySet().stream().filter(student-> student.getValue() > 90)
                .map(Map.Entry::getKey).forEach(System.out::println);
        }
//        // 我的问题2：请使用lambda表达式将所有学生的成绩都加5分（模拟加分）
//        System.out.println("\n问题2：给所有学生成绩加5分");
    @Test
    public void testAdd5Point(){
        Map<String, Integer> maps = studentScores.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> value.getValue() + 5));
        maps.forEach((k,v)->{
            System.out.println(k+":"+v);
        });
    }
//        // 我的问题3：请使用lambda表达式按成绩从高到低排序学生
//        System.out.println("\n问题3：按成绩从高到低排序学生");
            @Test
    public void testSortByScore(){
        studentScores.values().stream().sorted(Comparator.comparing(Integer::intValue).reversed()).forEach(System.out::println);
    }
//
//        // 我的问题4：请使用lambda表达式计算所有学生的平均成绩
//        System.out.println("\n问题4：计算所有学生的平均成绩");
            @Test
    public void testAvgScore(){
        studentScores.values().stream().mapToInt(Integer::intValue).average().ifPresent(System.out::println);
    }
//        // 我的问题5：请使用lambda表达式找出成绩最低的学生
//        System.out.println("\n问题5：找出成绩最低的学生");
    @Test
    public void testScoreMin(){
//        studentScores.entrySet().stream().min(Comparator.comparing(Map.Entry::getValue)).ifPresent(System.out::println);
        studentScores.entrySet().stream().min(Map.Entry.comparingByValue()).ifPresent(System.out::println);
    }



    //筛选A开头的名字,按照字母排序
    @Test
    public void testA(){
        List<String> namesList = names.stream().filter(s -> s.startsWith("A")).sorted().toList();
        log.info("namesList:{}",namesList);
    }

    @Test
    public void testCalc(){
        List<Integer> result = numbers.stream().filter(n -> n % 2 == 0).map(n -> n * n).filter(res -> res > 50).toList();
        log.info("result:{}",result);
    }

    @Test
    public void testMapStruct(){
        List<String> lists
                = productPrices.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(entry -> entry.getKey() + ": " + entry.getValue()).toList();
        log.info("lists:{}",lists);
    }

    //将 employees按 年龄是否大于等于 30 分成两组：>=30和 <30，并分别收集他们的名字。
    @Test
    public void testGroupByAge(){
        Map<Boolean, List<String>> collect = employees.stream().collect(Collectors.partitioningBy(n -> n.age >= 30, Collectors.mapping(Employee::getName, Collectors.toList())));
        collect.values().forEach(System.out::println);
    }

    //salary is max
    @Test
    public void testSalaryMax(){
        List<String> list = employees.stream().max(Comparator.comparing(Employee::getSalary)).stream().map(e -> e.getName() + ":" + e.getSalary()).toList();
        log.info("list:{}",list);
    }

    //去重 限制 只取前三个名字, 转为大写
    @Test
    public void testDis(){
        // 此时的去重是根据Employee对象的hashCode和equals方法来判断的
        //正确的应该是先拿到employee的name
        List<String> namesLists = employees.stream().map(Employee::getName).distinct().filter(name-> name.length() > 3).limit(3).map(String::toUpperCase).toList();
        log.info("namesLists:{}",namesLists);
    }
    //自定义排序

    @Test
    public void testSortEd(){
        List<String> employeess = employees.stream().sorted(Comparator.comparing(Employee::getSalary).reversed().thenComparingInt(Employee::getAge)).map(e -> e.getName() + "(" + e.getSalary() + ")").toList();
        log.info("employeess:{}",employeess);
    }


    @Test
    public void testFlatMap(){
        // 假设有一个 List<List<String>>，如 [["A", "B"], ["C", "D"], ["E"]]
        List<List<String>> nestedList = Arrays.asList(
                Arrays.asList("A", "B"),
                Arrays.asList("C", "D"),
                Arrays.asList("E")
        );
//        log.info("nestedList:{}",nestedList);
        // 使用 flatMap 展平嵌套的 List
        List<String> nsList = nestedList.stream().flatMap(List::stream).map(String::toUpperCase).toList();
        log.info("nsList:{}",nsList);

    }


    @Test
    public void testDistinct(){
       List<Integer> arrayList = Lists.newArrayList(5, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        List<Integer> listWithNoRe = arrayList.stream().distinct().collect(Collectors.toList());
        System.out.println(listWithNoRe);
    }




    }
