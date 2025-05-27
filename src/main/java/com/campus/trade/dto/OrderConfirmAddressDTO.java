package com.campus.trade.dto;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
@Data
public class OrderConfirmAddressDTO implements Serializable {
    @NotNull(message = "地址ID不能为空")
    private Integer addressId;
}