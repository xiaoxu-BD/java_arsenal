package org.xiaoxu.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum Week {

    // 实例常量 必须要写在首行;
    MONDAY(1,"星期一"),
    TUESDAY(2,"星期二"),
    WEDNESDAY(3,"星期三"),
    THURSDAY(4,"星期四"),
    FRIDAY(5,"星期五"),
    SATURDAY(6,"星期六"),
    SUNDAY(7,"星期日");

    private Week(Integer nums,String name){
        this.nums = nums;
        this.name = name;
    }
    private final Integer nums;
    private final String name;

    //根据num获取对应的枚举
    public static String getWeek(Integer num){
        for(Week week : Week.values()){
            if(Objects.equals(week.getNums(), num)){
                return week.getName();
            }
        }
        return null;
    }
}