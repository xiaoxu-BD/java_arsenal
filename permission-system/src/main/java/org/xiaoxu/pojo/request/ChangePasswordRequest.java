package org.xiaoxu.pojo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class ChangePasswordRequest {

    /**
     * 旧密码（首次登录时可以为空）
     */
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
