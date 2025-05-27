// src/main/java/com/campus/trade/mapper/CartMapper.java
package com.campus.trade.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.Cart;
import com.campus.trade.entity.Trade;
import com.campus.trade.vo.CartItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    List<CartItemVO> findCartItemsByUserId(@Param("userId") Integer userId);
}





