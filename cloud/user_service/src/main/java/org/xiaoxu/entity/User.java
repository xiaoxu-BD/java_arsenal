package org.xiaoxu.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @className: User
 * @author: xiaoxu
 * @date: 2025/8/3 13:28
 * @Version: 1.0
 * @description:
 */
@Data
public class User {
    private String id;
    private String name;
    private String password;
    private String avator;
    private String location;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer gender;
    private Integer delFlag;

}
