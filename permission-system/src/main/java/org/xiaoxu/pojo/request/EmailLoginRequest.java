package org.xiaoxu.pojo.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailLoginRequest {
    private String email;
    private String code;
}
