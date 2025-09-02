package org.xiaoxu.web_boot.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @className: UserInfo
 * @author: xiaoxu
 * @date: 2025/9/2 15:05
 * @Version: 1.0
 * @description:
 */

public class UserInfo {

    private String userId;
    private String gender;
    private String name;

    private String location;

    private String avator;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAvator() {
        return avator;
    }

    public void setAvator(String avator) {
        this.avator = avator;
    }
}
