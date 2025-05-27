package com.campus.trade.vo;
import com.campus.trade.enums.OrderStatusEnum;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class OrderVO implements Serializable { // 订单列表或详情展示用
    private Integer id;
    private String orderSn;
    private Integer buyerId;
    private Integer sellerId;
    private BigDecimal totalAmount;
    private OrderStatusEnum status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    // 关联信息
    private List<OrderItemVO> orderItems; // VO 列表
    private AddressVO address;          // VO 对象
    private String buyerNickname;
    private String sellerNickname;
}