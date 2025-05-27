package com.campus.trade.controller;

import com.campus.trade.dto.CartAddReqDTO;
import com.campus.trade.service.ICartService;
import com.campus.trade.utils.Result;
import com.campus.trade.vo.CartItemVO;
import lombok.extern.slf4j.Slf4j; // 引入 Slf4j
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j // 添加日志注解
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ICartService cartService;

    /**
     * 添加商品到购物车
     * @param addReqDTO 请求 DTO，包含 itemId
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> addToCart(@Validated @RequestBody CartAddReqDTO addReqDTO) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        try {
            cartService.addToCart(userId, addReqDTO.getItemId());
            return Result.success("已添加到购物车");
        } catch (Exception e) {
            log.error("添加购物车失败, userId={}, itemId={}", userId, addReqDTO.getItemId(), e);
            return Result.error(400, "添加失败: " + e.getMessage());
        }
    }

    /**
     * 获取购物车列表
     * @return 购物车项VO列表
     */
    @GetMapping
    public Result<List<CartItemVO>> getCartList() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        try {
            List<CartItemVO> cartList = cartService.getCartList(userId);
            return Result.success(cartList);
        } catch (Exception e) {
            log.error("获取购物车列表失败, userId={}", userId, e);
            // 返回空列表而不是错误，可能是更好的用户体验
            // return Result.error(500, "获取购物车失败");
            return Result.success(List.of()); // 返回空列表
        }
    }

    /**
     * 移除购物车项
     * @param cartItemId 购物车项 ID
     * @return 操作结果
     */
    @DeleteMapping("/remove/{cartItemId}")
    public Result<Void> removeFromCart(@PathVariable Integer cartItemId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        try {
            boolean success = cartService.removeFromCart(userId, cartItemId);
            if (success) {
                return Result.success("已从购物车移除");
            } else {
                log.warn("移除购物车项 Service 返回 false, cartItemId={}, userId={}", cartItemId, userId);
                return Result.error(404, "购物车项未找到或删除失败");
            }
        } catch (Exception e) {
            log.error("移除购物车项失败, cartItemId={}, userId={}", cartItemId, userId, e);
            return Result.error(400, "移除失败: " + e.getMessage());
        }
    }

    /**
     * 辅助方法获取当前登录用户的 User ID
     * @return 用户 ID，如果未登录或无法解析则返回 null
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof String) {
            try {
                return Integer.parseInt((String) authentication.getPrincipal());
            } catch (NumberFormatException e) {
                log.warn("无法将 Principal '{}' 解析为用户 ID (Integer)", authentication.getPrincipal(), e);
                return null;
            }
        }
        log.warn("无法获取认证信息或 Principal 不是 String 类型: {}", authentication);
        return null; // 未认证或 Principal 类型不符
    }
}