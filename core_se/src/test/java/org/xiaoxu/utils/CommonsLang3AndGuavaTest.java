package org.xiaoxu.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import junit.framework.TestCase;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.xiaoxu.entity.Person;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @className: ComonsLang3AndGuavaTest
 * @author: xiaoxu
 * @date: 2025/10/29 17:18
 * @Version: 1.0
 * @description:
 */
public class CommonsLang3AndGuavaTest extends TestCase {



    public void testStringU(){
        String str = "hello world";
        //是否为空? false => 有值  true => 空
         // 检查是否为null 或者为空 或者为空" "
        String str2 = " ";
        boolean res = StringUtils.isEmpty(str);
        boolean res1 = StringUtils.isEmpty(str2);
        System.out.println("字符串为空（不trim）" + res);
        System.out.println("字符串为空（不trim）" + res1);
    }

    public void testStringU2(){
        String str = "hello world";
        //是否为空? false => 有值  true => 空

        String str2 = "";
        String str3 = " ";
        boolean res = StringUtils.isBlank(str);
        boolean res1 = StringUtils.isBlank(str2);
        boolean res2 = StringUtils.isBlank(str3);
        System.out.println("字符串为空白字符串（trim）" + res);
        System.out.println("字符串为空白字符串（trim）" + res1);
        System.out.println("字符串为空白字符串（trim）" + res2);
    }

    public void testStringU3(){
        String str = "hello world";
        //是否为空? false => 有值  true => 空
        boolean res = StringUtils.isNotBlank(str);
        System.out.println("字符串不为空白字符串（trim）" + res);
    }

    public void testStringU4(){
        String str = "hello world";
        String[] result = StringUtils.split(str, " ");
        System.out.println(result[0]);
        System.out.println(result[1]);
    }


    public void testStringJoin(){
        List<String> strList = Arrays.asList("http:/", "localhost", "admin-api");
        //支持各种类型（数组、集合、可变参数等）和分隔符。
        String joinStr = StringUtils.join(strList,"/");


        String collect = strList.stream().collect(Collectors.joining("/"));
        System.out.println("Collectors.joining:" + collect);
        System.out.println(joinStr);
    }


    public void testStringU5(){
        List<Person> list = Arrays.asList(new Person("xiaoxu", 23), new Person("xiaozhang", 23));
        //支持前后缀
        String collect = list.stream().map(Person::getName).collect(Collectors.joining(",","[","]"));
        System.out.println(collect);
    }

    public void testStringU6(){
        String str = "A,B,C";
        String[] split = StringUtils.split(str, ",");
        split[0] = "D";
        System.out.println(split[0]);
    }

    public void testStringU7(){
        String str = "A";
        // 转小写
        System.out.println(StringUtils.lowerCase(str));
        //转大写
        StringUtils.upperCase( str);
    }


    public void testArraysUtils1(){
        String[] strArray = {"a","b","c"};
        System.out.println(ArrayUtils.isEmpty(strArray));
    }

    public void testArrayUtils2(){
        int [] intArray = {1,2,3};
        //支持基本类型的重载
        ArrayUtils.add(intArray,4);
    }


    public void testUtils3(){
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, MINUTES)
                .maximumSize(1000)
                .build();

        cache.put("key", "value");
        cache.put("key2", "value2");
        String key = cache.getIfPresent("key");
        System.out.println(key);
    }
    public  void testListU1(){
        List<String> stringList = Lists.newArrayList("a", "b", "c");
        for (String s : stringList) {
            System.out.println(s);
        }
        Map<String,String> objectObjectHashMap = Maps.newHashMap();

        HashSet<@Nullable Object> objects = Sets.newHashSet();
        ImmutableList<String> immutableList = ImmutableList.of("1", "2", "3");
    }

    public void testListU2(){

        //key为String ,value 为List<Integer>
        ArrayListMultimap<String, List<Integer>> objectObjectArrayListMultimap = ArrayListMultimap.create();
        objectObjectArrayListMultimap.put("key1", Lists.newArrayList(1,2,3));
        objectObjectArrayListMultimap.put("key1", Lists.newArrayList(4,5,6));
        objectObjectArrayListMultimap.put("key2", Lists.newArrayList(4,5,6));
        objectObjectArrayListMultimap.put("key3", Lists.newArrayList(9,8,2,11));
        List<List<Integer>> key1List = objectObjectArrayListMultimap.get("key1");
        System.out.println(key1List);

        HashBasedTable<String, String, String> stringStringStringHashBasedTable = HashBasedTable.create();
        stringStringStringHashBasedTable.put("key1","key2","value");
        stringStringStringHashBasedTable.put("key1","key3","value");
        System.out.println(stringStringStringHashBasedTable.get("key1", "key2"));
        System.out.println(stringStringStringHashBasedTable.get("key1", "key3"));
    }



    public void testCache() throws InterruptedException{
        Cache<String, String> cacheSide = CacheBuilder.newBuilder()
                .maximumSize(2).
                expireAfterWrite(3, TimeUnit.SECONDS).build();
//        expireAfterWrite 可以实现写入后 3 秒过期，但有个小问题：它并不是 3 秒后立即过期，
//     而是在 3 秒过后再次问一次时才能删除 k/v 值
//     expireAfterAccess 方法则是读取后 3 秒过期，如果 3 秒之内一直读取，那么这个数据就一直不会过期
        cacheSide.put("key1", "value1");
        cacheSide.put("key2", "value2");


        int time = 1;
        while (true){
            System.out.println("第" + time++ + "次获取到local Cache的值:"+ cacheSide.getIfPresent("key1") );
            if (time == 5){
                break;
            }
            Thread.sleep(1000);
        }

    }
}
