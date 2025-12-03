package org.xiaoxu.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum UserStatusEnum {

    INIT("0", "INIT"),


    AUTHENTICATED("1", "AUTHENTICATED"),


    LOCKED("2", "LOCKED"),


    DELETED("3", "DELETED"),


    DISABLED("4", "DISABLED"),


    EXPIRED("5", "EXPIRED"),


    UNKNOWN("6", "UNKNOWN"),

    ;



    private String code;
    private String desc;



    private UserStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserStatusEnum fromCode(String code) {
        for (UserStatusEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return UNKNOWN;
    }

    public static String fromDesc(String desc){
        for (UserStatusEnum value : UserStatusEnum.values()) {
            if (value.desc.equals(desc)) return value.code;
        }
        return "UNKNOWN";
    }

    public static void main(String[] args) {
        System.out.println(Arrays.asList(UserStatusEnum.values()));
        System.out.println(UserStatusEnum.fromDesc("EXPIRED"));
    }

}
