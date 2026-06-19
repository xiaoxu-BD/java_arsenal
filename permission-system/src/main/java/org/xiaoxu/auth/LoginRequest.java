package org.xiaoxu.auth;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank (message = "密码不能为空")
    private String password;
}
