package com.campus.trade.dto;
import lombok.Data;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
@Data
public class UserProfileDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname; // 可选更新

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号码格式不正确") // 允许为空或满足手机号正则
    private String phone; // 可选更新

    // 添加其他允许用户修改的字段...
}