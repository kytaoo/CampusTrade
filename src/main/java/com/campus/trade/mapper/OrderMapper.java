// src/main/java/com/campus/trade/mapper/OrderMapper.java
package com.campus.trade.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.dto.OrderQueryParam;
import com.campus.trade.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
     // 分页查询订单列表 (买家或卖家视角)
    IPage<Order> findOrderPage(Page<Order> page, @Param("queryParam") OrderQueryParam queryParam); // 使用复杂查询对象
}