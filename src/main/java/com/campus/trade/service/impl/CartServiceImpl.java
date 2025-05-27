package com.campus.trade.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.entity.Cart;
import com.campus.trade.entity.Item;
import com.campus.trade.mapper.CartMapper;
import com.campus.trade.mapper.ItemMapper;
import com.campus.trade.service.ICartService;
import com.campus.trade.vo.CartItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ItemMapper itemMapper;

    @Override
    public Cart addToCart(Integer userId, Integer itemId) throws Exception {
        // 1. 检查商品是否存在且在售
        Item item = itemMapper.selectById(itemId);
        if (item == null || !"在售".equals(item.getStatus())) {
            throw new Exception("商品不存在或已下架/售出");
        }
        // 2. 检查是否是自己的商品
        if(item.getUserId().equals(userId)){
            throw new Exception("不能添加自己的商品到购物车");
        }
        // 3. 检查购物车是否已存在该商品 (二手交易不允许重复添加)
        Cart existingCart = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getItemId, itemId));
        if (existingCart != null) {
            throw new Exception("该商品已在购物车中");
        }

        // 4. 添加到购物车
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItemId(itemId);
        // cart.setQuantity(1); // 如果有数量字段
        cartMapper.insert(cart);
        return cart;
    }

    @Override
    public List<CartItemVO> getCartList(Integer userId) {
        return cartMapper.findCartItemsByUserId(userId);
    }

    @Override
    public boolean removeFromCart(Integer userId, Integer cartItemId) throws Exception {
        Cart cart = cartMapper.selectById(cartItemId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new Exception("购物车项不存在或无权删除");
        }
        return cartMapper.deleteById(cartItemId) > 0;
    }

    @Override
    public boolean removeItemsFromCart(Integer userId, List<Integer> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return true;
        }
        // 批量删除，需要校验 userId
        int deletedCount = cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .in(Cart::getId, cartItemIds));
        return deletedCount > 0; // 或者根据业务需要判断是否全部删除成功
    }
}