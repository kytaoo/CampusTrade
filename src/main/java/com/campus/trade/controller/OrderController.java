package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.dto.OrderConfirmAddressDTO;
import com.campus.trade.dto.OrderCreateReqDTO;
import com.campus.trade.dto.OrderPayReqDTO;
import com.campus.trade.dto.OrderQueryParam;
import com.campus.trade.entity.Address; // 引入 Address
import com.campus.trade.entity.Order;
import com.campus.trade.entity.OrderItem; // 引入 OrderItem
import com.campus.trade.service.IOrderService;
import com.campus.trade.utils.Result;
import com.campus.trade.vo.AddressVO; // 引入 AddressVO
import com.campus.trade.vo.OrderItemVO; // 引入 OrderItemVO
import com.campus.trade.vo.OrderVO;
import lombok.extern.slf4j.Slf4j; // 引入 Slf4j
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils; // 引入 CollectionUtils
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j // 添加日志注解
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    /**
     * 创建订单 (处理购物车结算和立即购买)
     * @param createReqDTO 请求 DTO
     * @return 创建成功的订单VO列表 (待支付状态)
     */
    @PostMapping
    public Result<List<OrderVO>> createOrder(@RequestBody OrderCreateReqDTO createReqDTO) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        List<Order> createdOrders;
        try {
            if (!CollectionUtils.isEmpty(createReqDTO.getCartItemIds())) {
                // 从购物车创建
                createdOrders = orderService.createOrdersFromCart(userId, createReqDTO.getCartItemIds(), createReqDTO.getRemarkMap());
            } else if (createReqDTO.getItemId() != null) {
                // 立即购买
                Order order = orderService.createOrderFromBuyNow(userId, createReqDTO.getItemId(), createReqDTO.getRemark());
                createdOrders = new ArrayList<>(); // 使用可变列表
                createdOrders.add(order);
            } else {
                return Result.badRequest("请求参数错误，缺少购物车项或商品ID");
            }

            // 转换成 VO 返回给前端
            List<OrderVO> vos = mapOrdersToVOs(createdOrders);
            return Result.success("订单创建成功，请尽快支付", vos);

        } catch (Exception e) {
            log.error("创建订单失败, userId={}", userId, e);
            return Result.error(400, "创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 为订单设置收货地址
     * @param orderId 订单ID
     * @param confirmDto 包含 addressId 的 DTO
     * @return 操作结果
     */
    @PutMapping("/{orderId}/address")
    public Result<Void> setOrderAddress(@PathVariable Integer orderId, @Validated @RequestBody OrderConfirmAddressDTO confirmDto) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        try {
            orderService.setOrderAddress(userId, orderId, confirmDto.getAddressId());
            return Result.success("订单地址设置成功");
        } catch (Exception e) {
            log.error("设置订单地址失败, orderId={}, addressId={}, userId={}", orderId, confirmDto.getAddressId(), userId, e);
            return Result.error(400, "设置地址失败: " + e.getMessage());
        }
    }

    /**
     * 支付订单 (余额支付)
     * @param payReqDTO 包含 orderIds 列表的 DTO
     * @return 操作结果
     */
    @PostMapping("/pay")
    public Result<Void> payOrder(@Validated @RequestBody OrderPayReqDTO payReqDTO) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        try {
            orderService.payOrders(userId, payReqDTO.getOrderIds());
            return Result.success("支付成功！");
        } catch (Exception e) {
            log.error("支付订单失败, userId={}, orderIds={}", userId, payReqDTO.getOrderIds(), e);
            return Result.error(400, "支付失败: " + e.getMessage());
        }
    }

    /**
     * 卖家发货
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PutMapping("/{orderId}/ship")
    public Result<Void> shipOrder(@PathVariable Integer orderId) {
        Integer userId = getCurrentUserId(); // 获取的是当前操作用户，Service 层会校验是否为卖家
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        try {
            orderService.shipOrder(userId, orderId);
            return Result.success("发货成功");
        } catch (Exception e) {
            log.error("卖家发货失败, orderId={}, userId={}", orderId, userId, e);
            return Result.error(400, "发货失败: " + e.getMessage());
        }
    }

    /**
     * 买家确认收货
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PutMapping("/{orderId}/confirm")
    public Result<Void> confirmReceipt(@PathVariable Integer orderId) {
        Integer userId = getCurrentUserId(); // 获取的是当前操作用户，Service 层会校验是否为买家
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        try {
            orderService.confirmReceipt(userId, orderId);
            return Result.success("确认收货成功");
        } catch (Exception e) {
            log.error("确认收货失败, orderId={}, userId={}", orderId, userId, e);
            return Result.error(400, "确认收货失败: " + e.getMessage());
        }
    }

    /**
     * 买家取消订单 (仅限待付款)
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Integer orderId) {
        Integer userId = getCurrentUserId(); // 获取的是当前操作用户，Service 层会校验是否为买家
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        try {
            orderService.cancelOrder(userId, orderId);
            return Result.success("订单取消成功");
        } catch (Exception e) {
            log.error("取消订单失败, orderId={}, userId={}", orderId, userId, e);
            return Result.error(400, "取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 查询我的订单列表 (分页)
     * @param viewType 视角 ('buyer' 或 'seller')
     * @param status 订单状态 (中文, 可选)
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单分页结果VO
     */
    @GetMapping("/my")
    public Result<IPage<OrderVO>> listMyOrders(
            @RequestParam(defaultValue = "buyer") String viewType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        OrderQueryParam queryParam = new OrderQueryParam();
        queryParam.setUserId(userId);
        queryParam.setViewType(viewType);
        queryParam.setStatus(status);

        Page<Order> page = new Page<>(pageNum, pageSize);
        IPage<Order> orderPage = orderService.findOrders(page, queryParam);

        // 转换成分页的 VO
        IPage<OrderVO> voPage = orderPage.convert(this::mapOrderToVO); // 使用方法引用

        return Result.success(voPage);
    }

    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return 订单详情VO
     */
    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrderDetail(@PathVariable Integer orderId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        try {
            Order orderDetail = orderService.getOrderDetail(userId, orderId);
            // 转换为 VO
            OrderVO vo = mapOrderToVO(orderDetail);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取订单详情失败, orderId={}, userId={}", orderId, userId, e);
            // 如果是 Service 层抛出的 "无权查看" 等信息，返回 403 或 400 可能更合适
            if(e.getMessage().contains("无权")){
                return Result.forbidden("无权查看此订单");
            }
            return Result.error(404, "获取订单详情失败: " + e.getMessage());
        }
    }


    // --- 辅助方法 ---

    /**
     * 将 Order 实体转换为 OrderVO 视图对象
     * @param order Order 实体
     * @return OrderVO 视图对象
     */
    private OrderVO mapOrderToVO(Order order) {
        if (order == null) {
            return null;
        }
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo); // 复制基础属性

        // 转换 OrderItem 列表
        if (!CollectionUtils.isEmpty(order.getOrderItems())) {
            vo.setOrderItems(order.getOrderItems().stream().map(oi -> {
                OrderItemVO itemVO = new OrderItemVO();
                BeanUtils.copyProperties(oi, itemVO); // 假设字段匹配
                return itemVO;
            }).collect(Collectors.toList()));
        } else {
            vo.setOrderItems(new ArrayList<>()); // 保证不为 null
        }

        // 转换 Address
        if (order.getAddress() != null) {
            AddressVO addressVO = new AddressVO();
            BeanUtils.copyProperties(order.getAddress(), addressVO); // 假设字段匹配
            vo.setAddress(addressVO);
        }

        // buyerNickname 和 sellerNickname 应该在 Service 层查询并设置好了
        return vo;
    }

    /**
     * 将 List<Order> 转换为 List<OrderVO>
     * @param orders Order 实体列表
     * @return OrderVO 视图对象列表
     */
    private List<OrderVO> mapOrdersToVOs(List<Order> orders) {
        if (CollectionUtils.isEmpty(orders)) {
            return new ArrayList<>();
        }
        return orders.stream().map(this::mapOrderToVO).collect(Collectors.toList());
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