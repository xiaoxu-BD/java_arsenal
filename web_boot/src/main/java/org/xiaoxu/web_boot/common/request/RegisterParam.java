package org.xiaoxu.web_boot.common.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class RegisterParam {

    private Long id;

    @NotBlank(message = "身份证不能为空")
    private String idCard;

    @NotBlank(message = "请填写性别")
    private String sex;
}
