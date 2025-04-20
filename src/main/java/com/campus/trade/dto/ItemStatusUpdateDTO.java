package com.campus.trade.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class ItemStatusUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "状态不能为空")
    // 可以添加自定义注解校验状态值是否合法 ("ON_SALE", "OFF_SHELF")
    private String status;
}