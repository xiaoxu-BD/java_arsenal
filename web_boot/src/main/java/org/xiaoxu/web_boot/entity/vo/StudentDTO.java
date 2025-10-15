package org.xiaoxu.web_boot.entity.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @className: StudentDTO
 * @author: xiaoxu
 * @date: 2025/10/15 19:11
 * @Version: 1.0
 * @description:
 */
@Data
public class StudentDTO {


    @NotBlank(message = "用户名不能为空")
    private String name;

    @Size(min = 6, max = 20,message = "密码长度在6-20之间")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;
}
