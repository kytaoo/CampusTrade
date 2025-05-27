package com.campus.trade.dto;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
@Data
public class CartAddReqDTO implements Serializable {
    @NotNull(message = "商品ID不能为空")
    private Integer itemId;
}