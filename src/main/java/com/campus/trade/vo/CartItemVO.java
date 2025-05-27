package com.campus.trade.vo;
import com.campus.trade.entity.Item; // 引入 Item
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartItemVO implements Serializable { // 用于购物车列表展示
    private static final long serialVersionUID = 1L;
    private Integer id; // cart id
    private Integer userId;
    private Integer itemId;
    // 直接从关联查询获取商品信息
    private String itemTitle;
    private BigDecimal itemPrice;
    private List<String> itemImages; // 只取第一个用于展示
    private String itemStatus; // 商品当前状态

    // 如果需要展示完整的 Item 对象
    // private Item item;
}