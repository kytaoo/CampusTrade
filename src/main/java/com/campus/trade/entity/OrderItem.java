package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("order_item")
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("order_id")
    private Integer orderId; // 所属订单ID

    @TableField("item_id")
    private Integer itemId; // 商品ID (用于关联，但订单信息不应依赖它实时变化)

    // --- 商品快照信息 ---
    @TableField("item_title")
    private String itemTitle; // 商品标题快照

    @TableField("item_image")
    private String itemImage; // 商品主图快照 (存相对路径)

    @TableField("price")
    private BigDecimal price; // 下单时商品价格快照

    // 二手交易通常为 1
    // private Integer quantity;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}