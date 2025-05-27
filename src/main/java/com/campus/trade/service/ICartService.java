package com.campus.trade.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Cart;
import com.campus.trade.vo.CartItemVO;
import java.util.List;

public interface ICartService extends IService<Cart> {
    Cart addToCart(Integer userId, Integer itemId) throws Exception;
    List<CartItemVO> getCartList(Integer userId);
    boolean removeFromCart(Integer userId, Integer cartItemId) throws Exception;
    boolean removeItemsFromCart(Integer userId, List<Integer> cartItemIds); // 批量删除
}