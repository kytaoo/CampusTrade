package com.campus.trade.dto;

import lombok.Data;

@Data
public class OrderQueryParam {
    private Integer userId; // 当前操作的用户ID (必须)
    private String viewType = "buyer"; // 视角: "buyer" 或 "seller" (默认买家)
    private String status; // 订单状态 (可选, 使用 OrderStatusEnum 的 description)
    // 可以添加其他筛选条件，如时间范围等
}