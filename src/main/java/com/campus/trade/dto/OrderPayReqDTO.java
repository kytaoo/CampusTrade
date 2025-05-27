package com.campus.trade.dto;
import lombok.Data;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
@Data
public class OrderPayReqDTO implements Serializable {
    @NotEmpty(message = "请选择要支付的订单")
    private List<Integer> orderIds;
}