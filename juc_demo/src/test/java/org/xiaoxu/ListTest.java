package org.xiaoxu;

import cn.hutool.core.collection.CollUtil;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @className: ListTest
 * @author: xiaoxu
 * @date: 2025/6/8 17:00
 * @Version: 1.0
 * @description:
 */
@Slf4j
public class ListTest extends TestCase {


    public void testCollectionUtils(){
      List<String> list = CollUtil.newArrayList("1", "2", "3", "5");

        Set<String> collect = list.stream().map(item -> item + "_").collect(Collectors.toSet());

        collect.forEach(System.out::println);
    }

    /**
     * 测试DualHashBidiMap
     * 双向map 通过key 获取value
     * 或者通过value 获取key
     */
    public void testList(){
        DualHashBidiMap<String, String> bidiMap = new DualHashBidiMap<>();
        bidiMap.put("1", "1");
        bidiMap.put("2", "333");
        bidiMap.put("3", "3");
        bidiMap.put("4", "4");
        bidiMap.put("5", "5");
        System.out.println("通过key获取value: " + bidiMap.get("1"));
        System.out.println("通过value获取key: " + bidiMap.getKey("333"));
    }

    /**
     * 一个key 对应多个值
     * new ArrayListValuedHashMap<>();
     */
    public void testList2(){
        ArrayListValuedHashMap<String, Object> listValuedHashMap = new ArrayListValuedHashMap<>();
        listValuedHashMap.put("key1","val1");
        listValuedHashMap.put("key1","val2");
        listValuedHashMap.put("key1","val3");
        listValuedHashMap.put("key2","val4");
        listValuedHashMap.put("key2","val5");
        //获取值

        List<Object> values = listValuedHashMap.get("key1");
        if (CollectionUtils.isNotEmpty(values)){
            log.info("values: {} ",values);
        }
    }



}
