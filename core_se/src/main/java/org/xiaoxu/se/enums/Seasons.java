package org.xiaoxu.se.enums;


import lombok.Getter;

@Getter
public enum Seasons {


    SPRING("SPRING"),
    SUMMER("SUMMER")

    ;



    private String desc;

    private Seasons(String desc){
        this.desc = desc;
    }


    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "这是" + name();
    }
}
