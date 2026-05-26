package org.xiaoxu.se.foundation;

import org.apache.commons.lang3.StringUtils;

public class EqualsAndOtherDem {
    public static void main(String[] args) {


        System.out.println(otherEquals());
        System.out.println(equalsMySelf());


    }




    private static boolean otherEquals(){
//        本质是基本类型放在内存中直接就是存放的是值本身
        int i = 10;
        int b = 10;
        return i == b;
    }


    private static boolean equalsMySelf(){
//本质是引用类型方法内存中是一个地址值，String应该还有字符串常量池把
        String s = "str";
        String s2 = "str";
        return StringUtils.equals(s,s2);
    }
}
