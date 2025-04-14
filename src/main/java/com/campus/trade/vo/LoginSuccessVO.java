package com.campus.trade.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class LoginSuccessVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String token;       // JWT
    private String tokenHead;   // Token 前缀 (例如 "Bearer ")
    private Integer userId;     // 用户 ID
    private String nickname;    // 用户昵称
    private String avatar;      // 用户头像 URL (可能为 null)
}