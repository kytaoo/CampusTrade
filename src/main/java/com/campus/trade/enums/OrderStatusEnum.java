package com.campus.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum OrderStatusEnum {
    PENDING_PAYMENT("待付款"),
    PENDING_SHIPMENT("待发货"), // 支付成功后
    PENDING_RECEIPT("待收货"),  // 卖家发货后
    COMPLETED("已完成"),      // 买家确认收货后
    CANCELLED("已取消");      // 待付款时取消或超时取消

    // 注意：数据库 ENUM 定义需要与这里的 description 匹配
    @EnumValue // Mybatis Plus 默认映射 description 到数据库 ENUM
    private final String description;

    OrderStatusEnum(String description) {
        this.description = description;
    }
}