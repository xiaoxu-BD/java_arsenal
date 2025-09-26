package org.xiaoxu.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @className: CollectionUtils
 * @author: xiaoxu
 * @date: 2025/9/25 14:42
 * @Version: 1.0
 * @description:
 */
public class CollectionUtils {

    public static <T, U> List<U> convertList(T[] from, Function<T, U> func) {
        if (ArrayUtil.isEmpty(from)) {
            return new ArrayList<>();
        }
        return convertList(Arrays.asList(from), func);
    }

    public static <T, U> List<U> convertList(Collection<T> from, Function<T, U> func) {
        if (CollUtil.isEmpty(from)) {
            return new ArrayList<>();
        }
        return from.stream().map(func).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
