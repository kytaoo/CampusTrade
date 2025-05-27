package com.campus.trade.vo;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class UserInfoVO implements Serializable { // 用于返回给前端的用户信息
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String studentId;
    // 不包含 password
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private String status; // 可以返回状态描述
    private BigDecimal balance; // 可能不需要在这里返回，取决于业务
    private LocalDateTime createdAt;
}