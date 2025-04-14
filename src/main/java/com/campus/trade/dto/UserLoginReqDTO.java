package com.campus.trade.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class UserLoginReqDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "学号或邮箱不能为空")
    private String identifier; // 允许用户使用学号或邮箱登录

    @NotBlank(message = "密码不能为空")
    private String password;
}