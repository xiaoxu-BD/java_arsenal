package org.xiaoxu.web_boot.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class BeanUtil {

    /**
     * 拷贝单个对象属性
     *
     * @param source      源对象
     * @param targetClass 目标对象 class
     * @param <S>         源对象类型
     * @param <T>         目标对象类型
     * @return 拷贝后的目标对象
     */
    public static <S, T> T copyObject(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象拷贝失败", e);
        }
    }

    /**
     * 拷贝对象集合
     *
     * @param sourceList  源对象集合
     * @param targetClass 目标对象 class
     * @param <S>         源对象类型
     * @param <T>         目标对象类型
     * @return 拷贝后的目标对象集合
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> targetList = new ArrayList<>(sourceList.size());
        for (S source : sourceList) {
            targetList.add(copyObject(source, targetClass));
        }
        return targetList;
    }
}
