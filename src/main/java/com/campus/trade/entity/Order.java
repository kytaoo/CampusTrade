package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;

import com.campus.trade.enums.OrderStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "`order`", autoResultMap = true) // order 是 SQL 关键字，加反引号
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("order_sn")
    private String orderSn; // 订单号

    @TableField("buyer_id")
    private Integer buyerId; // 买家ID

    @TableField("seller_id")
    private Integer sellerId; // 卖家ID (一个订单对应一个卖家)

    @TableField("total_amount")
    private BigDecimal totalAmount; // 订单总金额

    @TableField("address_id")
    private Integer addressId; // 收货地址ID

    @TableField("status")
    private OrderStatusEnum status; // 订单状态枚举

    private String remark; // 订单备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 下单时间

    @TableField("paid_at")
    private LocalDateTime paidAt; // 支付时间

    @TableField("shipped_at")
    private LocalDateTime shippedAt; // 发货时间

    @TableField("completed_at")
    private LocalDateTime completedAt; // 完成时间

    @TableField("cancelled_at")
    private LocalDateTime cancelledAt; // 取消时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt; // 最后更新时间

    // --- 非数据库字段 ---
    @TableField(exist = false)
    private List<OrderItem> orderItems; // 订单包含的商品项列表
    @TableField(exist = false)
    private Address address; // 订单关联的收货地址信息
    @TableField(exist = false)
    private String buyerNickname; // 买家昵称 (可选，列表展示用)
    @TableField(exist = false)
    private String sellerNickname; // 卖家昵称 (可选，列表展示用)

}