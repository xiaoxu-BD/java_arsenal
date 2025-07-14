package org.xiaoxu.web_boot;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @className: TestStreamAPI
 * @author: xiaoxu
 * @date: 2025/6/29 19:13
 * @Version: 1.0
 * @description:
 */
public class TestStreamAPI {

    @Test
    public void testStreamAPI() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 25, null);
//
//
//        Map<String, List<String>> groupedNumbers = numbers.stream()
//                .filter(Objects::nonNull)
//                .filter(n -> n % 2 == 0)
//                .collect(Collectors.groupingBy(
//                    n -> "Group " + (n / 5),  // 直接在分组时添加"Group "前缀
//                    Collectors.mapping(Integer::toString, Collectors.toList())
//                ));
//
//        // 打印结果用于测试验证
//        groupedNumbers.forEach((k, v) -> System.out.println(k + ": " + v));
    }



    @Test
    public void testMethodFrame() {
        //局部变量?
        int i = 1;
        //方法参数?
        int j = 2;

    }
    }

