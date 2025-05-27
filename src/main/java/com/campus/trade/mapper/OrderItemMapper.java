// src/main/java/com/campus/trade/mapper/OrderItemMapper.java
package com.campus.trade.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.OrderItem;
import com.campus.trade.entity.Trade;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    // 可按需添加自定义查询
}

