package com.campus.trade.dto;
import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
@Data
public class OrderCreateReqDTO implements Serializable {
    private List<Integer> cartItemIds; // 从购物车结算时传递
    private Integer itemId;           // 直接购买时传递
    private Map<Integer, String> remarkMap; // (可选) 按卖家ID分的订单备注 {sellerId: "remark"}
    private String remark;             // 直接购买时的备注
    // 可以添加字段区分是哪种创建方式，或者让 Controller 根据参数判断
}