package org.xiaoxu.wecommail.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetail {
    private Long userId;
    private String username;
    private String nickname;
    private String email;
}