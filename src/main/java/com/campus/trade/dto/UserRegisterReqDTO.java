package com.campus.trade.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class UserRegisterReqDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "学号不能为空")
    @Size(min = 5, max = 20, message = "学号长度必须在 5 到 20 位之间")
    private String studentId;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6 到 20 位之间")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}