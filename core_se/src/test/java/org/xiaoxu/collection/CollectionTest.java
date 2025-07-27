package org.xiaoxu.collection;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.xiaoxu.entity.Person;
import org.xiaoxu.entity.Student;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * @className: CollectionTest
 * @author: xiaoxu
 * @date: 2025/5/24 19:58
 * @Version: 1.0
 * @description:
 */
@Slf4j
public class CollectionTest extends TestCase {
    public void testIfNullOrEmpty(){
        log.info("Objects.isNull()仅检查 null,如果对象是集合（如 List），需要额外检查 isEmpty()。\n" +
                "如果对象是字符串，需要额外检查 isEmpty() 或 isBlank()");
        //基本数据类型判断为空:Objects.isNull() 不为空: Objects.nonNull()
        // 判断为空: StringUtils.isBlank() 不为空: StringUtils.isNotBlank()
        //集合类型不为空:  !CollectionUtils.isEmpty()　为空: CollectionUtils.isEmpty()
        // map对象不为空: MapUtils.isNotEmpty() 为空: MapUtils.isEmpty()
        Student student = new Student();
        if (StringUtils.isNotBlank(student.getId())){
            log.info("开始更新学生信息");
        }
        log.info("开始新增学生信息");

    }

    public void testMap(){
        Map<String, Integer> studentScores = new HashMap<>();

        // 2. 添加元素 (put)
        studentScores.put("Alice", 95);
        studentScores.put("Bob", 88);
        studentScores.put("Charlie", 72);
        studentScores.put("David", 95); // 值可以重复
        studentScores.put("Alice", 100); // 如果键已存在，新值会替换旧值
        log.info("Map: {}", studentScores);
        Set<String> keys = studentScores.keySet();
        System.out.println("\n遍历键 (keySet()):");
        for (String key : keys) {
            Integer value = studentScores.get(key);
            System.out.println("键: " + key + ", 值: " + value);
        }

        Set<Map.Entry<String, Integer>> entries = studentScores.entrySet();
        System.out.println("\n遍历键值对 (entrySet() - 推荐):");
        for (Map.Entry<String, Integer> entry : entries) {
            System.out.println("键: " + entry.getKey() + ", 值: " + entry.getValue());
        }
//        studentScores.forEach((k,v)->log.info("{} : {}",k,v));

    }

    public void testMap2(){
        Map<String, Integer> studentScores = new HashMap<>();
        studentScores.put("Alice", 95);
        studentScores.put("Bob", 88);
        studentScores.put("Charlie", 72);
        studentScores.put("David", 95);
        for (Map.Entry<String, Integer> entry : studentScores.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            log.info("键: {}, 值: {}", key, value);
        }

        String ids = "1,2,3,4";
        List<String> list = Arrays.stream(ids.split(",")).map(String::trim).toList();
        log.info("{}",list);
    }

    public void testParseMethod(){
        String ids = "1,2,3,4";
        List<Long> list = Arrays.stream(ids.split(",")).map(Long::parseLong).toList();
        List<Integer> list2 = Arrays.stream(ids.split(",")).map(Integer::parseInt).toList();
        log.info("{}",list);
        log.info("{}",list2);
    }

    public void testLinedHashMap(){
        //LinkedHashMap 使用了一对双向链表来记录添加元素的先后顺序，可以保证遍历元素时，与添加的顺序一致。
        LinkedHashMap<String, Integer> studentScores = new LinkedHashMap<>();
        studentScores.put("Alice", 95);
        studentScores.put("Bob", 88);
        studentScores.put("Charlie", 72);
        studentScores.put("David", 95);
        for (Map.Entry<String, Integer> entry : studentScores.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            log.info("键: {}, 值: {}", key, value);
        }
    }

    public void testLinkedHashSet(){
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add("Alice");
        linkedHashSet.add("Bob");
        linkedHashSet.add("Charlie");
        linkedHashSet.add("David");
        linkedHashSet.add("Alice"); // 添加重复元素不会重复添加
        log.info("{}",linkedHashSet);
    }

    public void testBlockingQueue(){
        BlockingQueue<String> arrayQueue = new ArrayBlockingQueue<>(3);
        arrayQueue.add("a");
        arrayQueue.add("b");
        arrayQueue.add("c");
        log.info("{}",arrayQueue);
        arrayQueue.add("d");
        log.info("我被阻塞了");
    }
    public void testBlockingQueue2(){

        //两把锁
        BlockingQueue<String> linkedQueue = new LinkedBlockingQueue<>(3);
        linkedQueue.add("a");
        linkedQueue.add("b");
        linkedQueue.add("c");
        log.info("{}",linkedQueue);
        linkedQueue.add("d");
        log.info("我也被阻塞了");
    }


    public void testToMap(){
        List<Student> students = Arrays.asList(
                new Student("1", "Alice", 20),
                new Student("2", "Bob", 22),
                new Student("3", "Charlie", 21)
        );
        Map<String, Student> mapStudent = students.stream().collect(Collectors.toMap(Student::getId, s->s));

        Student student = mapStudent.get("1");
        log.info("{}",student);
    }

    public void testComparable(){
        Student student1 = new Student("1","Alice", 20);
        Student student2 = new Student("2","Bob", 20);
        assertTrue(student1.compareTo(student2) < 0);
    }


    /**
     * 测试学生姓名和年龄都相同的情况
     */
    public void testCompareTo_NamesAndAgesEqual() {
        Student student1 = new Student("1", "Alice", 20);
        Student student2 = new Student("2", "Alice", 20);
        assertEquals(0, student1.compareTo(student2));
    }



    public void testCompareToByComparator() {
        List<Person> personList = Arrays.asList(new Person("Alice", 20), new Person("Bob", 20), new Person("Charlie", 21));
        //按照名字排序 名字相同  按照年龄排序
        Collections.sort(personList, Comparator.comparing(Person::getName,Comparator.reverseOrder()).thenComparingInt(Person::getAge));
        log.info("{}", personList);

    }


    public void testModifyCurrent() {
        List<Person> personList = Arrays.asList(new Person("Alice", 20), new Person("Bob", 20), new Person("Charlie", 21));
    }
    //HashSet —— 快速去重判断是否访问
//    你只关心某个资源是否被访问过（不关心顺序、不追踪频率）；
//    比如：防止用户重复点击同一个按钮。
    public void testHashSet(){
        Set<Integer> accessedResources = new HashSet<>();
        accessedResources.add(1001);
        accessedResources.add(1002);
        //Collections.contains()
        boolean accessed = accessedResources.contains(1001);
        assertTrue(accessed);
    }

    //LinkedHashSet —— 去重 + 保留访问顺序
//    你关心某个资源是否被访问过，并且需要追踪访问顺序；
//    比如：实现一个LRU（Least Recently Used）缓存。
    public void testLinkedHashSet2() {
        Set<Integer> accessedResources = new LinkedHashSet<>();
        accessedResources.add(1001);
        accessedResources.add(1002);
        accessedResources.add(1003);
        for (Integer accessedResource : accessedResources) {
            log.info("{}",accessedResource);
        }
        BitSet bitSet = new BitSet(100);
        bitSet.set(100);
        bitSet.set(101);
        bitSet.set(102);
        boolean b = bitSet.get(100);
        log.info("{}", b);

    }


    public void testHash(){
        Class<Object> objectClass = Object.class;
        int i = objectClass.hashCode();
        log.info("{}", i);
    }


    public void testMap11(){
        Map<String,String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value1");
        map.put("key3", "value1");
    }


    public void testFloat(){
        System.out.println(0.1 + 0.2);
    }


    public void testBigDecimal(){
        BigDecimal b1 = BigDecimal.valueOf(1.0);
        BigDecimal b2 = BigDecimal.valueOf(1);
        //由于Long int 都是整数, scale精度都是为0 所以比较结果为true
        //但是valueOf方法会将传入的参数转换成String 类型就相当于: new BigDecimal("1.0")
        System.out.println(b1.equals(b2));
        //比较大小
        //b1 大于 b2 返回 1
        //b1 小于 b2 返回 -1
        //b1 等于 b2 返回 0
        System.out.println(b1.compareTo(b2));
    }

}
