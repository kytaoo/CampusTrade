package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.trade.enums.TradeTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("trade")
public class Trade implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId; // 交易所属用户ID

    @TableField("order_id")
    private Integer orderId; // 关联的订单ID (消费或收入时)

    private BigDecimal amount; // 交易金额 (正数:充值/收入, 负数:消费/支出)

    @TableField("balance_after")
    private BigDecimal balanceAfter; // 交易后的余额

    @TableField("trade_type")
    private TradeTypeEnum tradeType; // 交易类型枚举

    private String description; // 交易描述 (例如：购买商品 "商品标题...")

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 交易时间
}