package org.xiaoxu.wecommail.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfo {
    private Long userId;
    private String userName;
    private String email;
    private Integer age;
}