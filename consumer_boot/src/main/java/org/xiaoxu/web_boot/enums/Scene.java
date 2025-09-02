package org.xiaoxu.web_boot.enums;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;

@Getter
public enum Scene {



    WEChat("weixin","pay"),
    ALiPay("zhifubao","pay");




    private String message;
    private String description;


    Scene(String message, String description) {
        this.message = message;
        this.description = description;
    }


    public static Scene getScene(String message){
        Scene[] values = Scene.values();
        for(Scene value : values){
            if(Strings.isNotBlank(message) && value.getMessage().equals(message)){
                return value;
            }
        }
        return null;
    }
}
