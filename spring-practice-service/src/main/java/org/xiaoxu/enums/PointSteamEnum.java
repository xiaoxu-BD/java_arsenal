package org.xiaoxu.enums;

import lombok.Getter;

@Getter
public enum PointSteamEnum {

    SIGN_IN("SIGN_IN"),


    CONSUME("CONSUME"),

    INVITE("INVITE");



    private String desc;


    private PointSteamEnum(String desc){
        this.desc = desc;
    }
}
