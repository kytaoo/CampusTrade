package com.campus.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum TradeTypeEnum {
    RECHARGE("充值"), // 假设有充值功能
    CONSUMPTION("消费"), // 买家支付订单
    INCOME("收入"); // 卖家收到货款

    @EnumValue
    private final String description;

    TradeTypeEnum(String description) {
        this.description = description;
    }
}