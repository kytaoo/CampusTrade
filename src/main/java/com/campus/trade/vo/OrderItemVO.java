// src/main/java/com/campus/trade/vo/OrderItemVO.java
package com.campus.trade.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime; // 可以加上创建时间

@Data
public class OrderItemVO implements Serializable { // 订单项展示用
    private static final long serialVersionUID = 1L;

    private Integer id;         // 订单项 ID
    private Integer itemId;     // 关联的原始商品 ID
    private String itemTitle;   // 商品标题快照
    private String itemImage;   // 商品主图快照 (相对路径)
    private BigDecimal price;   // 下单时商品价格快照
    // private Integer quantity; // 如果有数量字段
    private LocalDateTime createdAt; // 可以加上创建时间
}