package org.xiaoxu.collection;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.xiaoxu.entity.Student;

import java.util.*;

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
}
