package org.xiaoxu.auth;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @className: LoginRequest
 * @author: xiaoxu
 * @date: 2025/11/17 16:46
 * @Version: 1.0
 * @description:
 */
@Getter
@Setter
public class LoginRequest implements Serializable {

    private String username;

    private String password;
}
