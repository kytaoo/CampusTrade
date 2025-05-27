package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.dto.OrderQueryParam;
import com.campus.trade.entity.*;
import com.campus.trade.enums.OrderStatusEnum;
import com.campus.trade.enums.TradeTypeEnum;
import com.campus.trade.mapper.*;
import com.campus.trade.service.*;
import com.campus.trade.utils.OrderSnGenerator; // 需要创建这个工具类
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private UserMapper userMapper; // 用于操作余额
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private ITradeService tradeService; // 用于记录流水
    @Autowired
    private ICartService cartService; // 用于删除购物车项
    @Autowired
    private IItemService itemService; // 用于更新商品状态


    @Override
    @Transactional // 涉及多表操作，必须加事务
    public List<Order> createOrdersFromCart(Integer userId, List<Integer> cartItemIds, Map<Integer, String> remarkMap) throws Exception {
        if (CollectionUtils.isEmpty(cartItemIds)) {
            throw new Exception("请选择要结算的商品");
        }

        // 1. 查询选中的购物车项及商品信息
        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .in(Cart::getId, cartItemIds)
                .eq(Cart::getUserId, userId));
        if (carts.size() != cartItemIds.size()) {
            throw new Exception("部分购物车项无效或不属于您");
        }

        List<Integer> itemIds = carts.stream().map(Cart::getItemId).collect(Collectors.toList());
        // 查出对应的商品信息，并用 Map 方便查找
        Map<Integer, Item> itemMap = itemMapper.selectBatchIds(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, item -> item));

        // 2. 按卖家 ID 分组商品项
        Map<Integer, List<Cart>> sellerCartMap = carts.stream().collect(Collectors.groupingBy(cart -> {
            Item item = itemMap.get(cart.getItemId());
            if (item == null) {
                throw new RuntimeException("购物车中商品ID " + cart.getItemId() + " 未找到"); // 应该有，否则数据有问题
            }
            // 校验商品状态，确保都是在售
            if (!"在售".equals(item.getStatus())) {
                 throw new RuntimeException("商品 [" + item.getTitle() + "] 已下架或售出，无法下单");
            }
             // 校验不能购买自己的商品
             if (item.getUserId().equals(userId)) {
                  throw new RuntimeException("不能购买自己发布的商品 [" + item.getTitle() + "]");
             }
            return item.getUserId(); // 按卖家的 userId 分组
        }));

        // 3. 为每个卖家创建订单
        List<Order> createdOrders = new ArrayList<>();
        for (Map.Entry<Integer, List<Cart>> entry : sellerCartMap.entrySet()) {
            Integer sellerId = entry.getKey();
            List<Cart> sellerCarts = entry.getValue();

            Order order = new Order();
            order.setOrderSn(OrderSnGenerator.generateOrderSn()); // 生成唯一订单号
            order.setBuyerId(userId);
            order.setSellerId(sellerId);
            order.setStatus(OrderStatusEnum.PENDING_PAYMENT); // 初始状态：待付款
            order.setRemark(remarkMap != null ? remarkMap.get(sellerId) : null); // 设置备注

            // 计算订单总金额，并创建 OrderItem
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();
            for (Cart cart : sellerCarts) {
                Item item = itemMap.get(cart.getItemId());
                totalAmount = totalAmount.add(item.getPrice()); // 累加价格

                // 创建 OrderItem (商品快照)
                OrderItem orderItem = new OrderItem();
                orderItem.setItemId(item.getId());
                orderItem.setItemTitle(item.getTitle());
                // 取商品 images 列表的第一个作为主图快照
                orderItem.setItemImage((item.getImages() != null && !item.getImages().isEmpty()) ? item.getImages().get(0) : null);
                orderItem.setPrice(item.getPrice());
                // orderItem.setQuantity(cart.getQuantity()); // 如果有数量
                orderItems.add(orderItem);
            }
            order.setTotalAmount(totalAmount);

            // 保存 Order
            orderMapper.insert(order); // 插入后 order 对象会获得 id

            // 批量保存 OrderItem，并设置 orderId
            for (OrderItem oi : orderItems) {
                oi.setOrderId(order.getId());
            }
            // Mybatis Plus 提供了批量插入的方法，但 ServiceImpl 里没有直接暴露，需要自己写或循环插入
            orderItems.forEach(orderItemMapper::insert);

            order.setOrderItems(orderItems); // 方便返回时携带
            createdOrders.add(order);
        }

        // 4. 清空已结算的购物车项
        cartService.removeItemsFromCart(userId, cartItemIds);

        return createdOrders;
    }

    @Override
    @Transactional
    public Order createOrderFromBuyNow(Integer userId, Integer itemId, String remark) throws Exception {
        // 1. 查询商品信息
        Item item = itemMapper.selectById(itemId);
        if (item == null || !"在售".equals(item.getStatus())) {
            throw new Exception("商品不存在或已下架/售出");
        }
        // 校验不能购买自己的商品
        if (item.getUserId().equals(userId)) {
             throw new Exception("不能购买自己发布的商品");
        }

        // 2. 创建 Order
        Order order = new Order();
        order.setOrderSn(OrderSnGenerator.generateOrderSn());
        order.setBuyerId(userId);
        order.setSellerId(item.getUserId());
        order.setStatus(OrderStatusEnum.PENDING_PAYMENT);
        order.setTotalAmount(item.getPrice());
        order.setRemark(remark);
        orderMapper.insert(order);

        // 3. 创建 OrderItem
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setItemId(item.getId());
        orderItem.setItemTitle(item.getTitle());
        orderItem.setItemImage((item.getImages() != null && !item.getImages().isEmpty()) ? item.getImages().get(0) : null);
        orderItem.setPrice(item.getPrice());
        orderItemMapper.insert(orderItem);

        order.setOrderItems(List.of(orderItem)); // 设置关联
        return order;
    }

    @Override
    @Transactional
    public Order setOrderAddress(Integer userId, Integer orderId, Integer addressId) throws Exception {
        Order order = orderMapper.selectById(orderId);
        // 校验订单
        validateOrderOperation(order, userId, OrderOperationType.SET_ADDRESS);
        // 校验地址
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new Exception("收货地址不存在或不属于您");
        }

        order.setAddressId(addressId);
        orderMapper.updateById(order);
        order.setAddress(address); // 返回时带上地址信息
        return order;
    }

    @Override
    @Transactional // 核心事务：扣款、改订单状态、改商品状态、记录流水
    public List<Order> payOrders(Integer userId, List<Integer> orderIds) throws Exception {
        if (CollectionUtils.isEmpty(orderIds)) {
            throw new Exception("请选择要支付的订单");
        }

        // 1. 查询用户信息和订单信息
        // 【注意并发】查询用户余额并锁定用户行，防止并发支付问题
        // User user = userMapper.selectByIdForUpdate(userId); // 需要自定义 Mapper 方法 + FOR UPDATE
        // 简单起见，先直接查询，但在高并发下有风险
        User user = userMapper.selectById(userId);
        if (user == null) throw new Exception("用户不存在");

        List<Order> ordersToPay = orderMapper.selectBatchIds(orderIds);
        if (ordersToPay.size() != orderIds.size()) {
             log.warn("部分订单未找到，支付中止。请求支付: {}, 实际找到: {}", orderIds, ordersToPay.stream().map(Order::getId).collect(Collectors.toList()));
             throw new Exception("部分订单不存在，请刷新后重试");
        }


        BigDecimal totalPaymentAmount = BigDecimal.ZERO;
        List<Integer> allItemIds = new ArrayList<>(); // 收集所有涉及的商品ID

        // 2. 校验订单状态和计算总金额
        for (Order order : ordersToPay) {
            // 必须是当前用户的订单
            if (!order.getBuyerId().equals(userId)) {
                 log.error("用户 {} 尝试支付不属于自己的订单 {}", userId, order.getId());
                 throw new Exception("无法支付不属于您的订单");
            }
            // 必须是待付款状态
            if (order.getStatus() != OrderStatusEnum.PENDING_PAYMENT) {
                 throw new Exception("订单 " + order.getOrderSn() + " 状态不是待付款，无法支付");
            }
            totalPaymentAmount = totalPaymentAmount.add(order.getTotalAmount());
            // 获取订单下的商品项 ID
            List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            items.forEach(oi -> allItemIds.add(oi.getItemId()));
        }

        // 3. 校验余额
        if (user.getBalance().compareTo(totalPaymentAmount) < 0) {
            throw new Exception("余额不足，当前余额：" + user.getBalance() + "，需要支付：" + totalPaymentAmount);
        }

        // 4. 扣除用户余额
        BigDecimal newBalance = user.getBalance().subtract(totalPaymentAmount);
        // 【注意并发】更新余额，最好使用乐观锁或数据库原子操作
        // userMapper.updateBalanceWithVersion(userId, newBalance, user.getVersion()); // 需要 version 字段和 Mapper 方法
        // 简单更新
        user.setBalance(newBalance);
        int userUpdateResult = userMapper.updateById(user);
        if (userUpdateResult == 0) {
             log.error("扣款失败，更新用户余额时记录数为 0 (可能并发导致)，用户ID: {}", userId);
             throw new Exception("支付失败，请稍后重试 (并发)"); // 或者使用乐观锁重试机制
        }


        // 5. 更新订单状态、商品状态、记录流水
        LocalDateTime now = LocalDateTime.now();
        List<Item> itemsToUpdate = new ArrayList<>();
        for (Order order : ordersToPay) {
            // 更新订单状态
            order.setStatus(OrderStatusEnum.PENDING_SHIPMENT); // 变为待发货
            order.setPaidAt(now);
            orderMapper.updateById(order);

            // 记录买家消费流水
            // 获取订单下的第一个商品标题用于描述
            OrderItem firstItem = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()).last("LIMIT 1")
            ).stream().findFirst().orElse(null);
            String tradeDesc = "购买商品";
            if(firstItem != null){
                tradeDesc += " [" + firstItem.getItemTitle().substring(0, Math.min(firstItem.getItemTitle().length(), 15)) + "...]";
            }

            tradeService.recordTrade(userId, order.getId(), order.getTotalAmount().negate(), newBalance, TradeTypeEnum.CONSUMPTION, tradeDesc);

            // 准备更新商品状态 (稍后批量更新)
            List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            for(OrderItem oi : orderItems){
                 Item item = new Item();
                 item.setId(oi.getItemId());
                 item.setStatus("已售"); // 更新为已售
                 itemsToUpdate.add(item);
            }
        }

        // 6. 批量更新商品状态为 "已售"
        if (!itemsToUpdate.isEmpty()) {
            itemService.updateBatchById(itemsToUpdate); // 使用 MP 的批量更新
        }

        log.info("用户 {} 成功支付订单: {}, 总金额: {}", userId, orderIds, totalPaymentAmount);
        return ordersToPay; // 返回支付成功的订单
    }

    @Override
    @Transactional
    public Order shipOrder(Integer userId, Integer orderId) throws Exception {
        Order order = orderMapper.selectById(orderId);
        validateOrderOperation(order, userId, OrderOperationType.SHIP);

        order.setStatus(OrderStatusEnum.PENDING_RECEIPT); // 变为待收货
        order.setShippedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return order;
    }

    @Override
    @Transactional // 确认收货：改订单状态、给卖家加钱、记录卖家流水
    public Order confirmReceipt(Integer userId, Integer orderId) throws Exception {
        Order order = orderMapper.selectById(orderId);
        validateOrderOperation(order, userId, OrderOperationType.CONFIRM_RECEIPT);

        // 1. 更新订单状态
        order.setStatus(OrderStatusEnum.COMPLETED); // 变为已完成
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 2. 给卖家增加余额
        Integer sellerId = order.getSellerId();
        User seller = userMapper.selectById(sellerId); // 【注意并发】这里也可能需要锁
        if (seller != null) {
            BigDecimal sellerNewBalance = seller.getBalance().add(order.getTotalAmount());
            seller.setBalance(sellerNewBalance);
            int sellerUpdateResult = userMapper.updateById(seller);
            if(sellerUpdateResult == 0){
                log.error("确认收货：给卖家 {} 加款失败 (并发)", sellerId);
                // 这里可以选择抛异常回滚，或者记录异常稍后处理
                // 简单处理：继续执行，但记录错误
            } else {
                 // 3. 记录卖家收入流水
                 OrderItem firstItem = orderItemMapper.selectList(
                     new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()).last("LIMIT 1")
                 ).stream().findFirst().orElse(null);
                 String tradeDesc = "售出商品";
                 if(firstItem != null){
                     tradeDesc += " [" + firstItem.getItemTitle().substring(0, Math.min(firstItem.getItemTitle().length(), 15)) + "...]";
                 }
                 tradeService.recordTrade(sellerId, orderId, order.getTotalAmount(), sellerNewBalance, TradeTypeEnum.INCOME, tradeDesc);
            }
        } else {
             log.error("确认收货：未找到卖家 {}，无法加款", sellerId);
        }

        return order;
    }

    @Override
    @Transactional // 取消订单：改订单状态、恢复商品状态
    public Order cancelOrder(Integer userId, Integer orderId) throws Exception {
        Order order = orderMapper.selectById(orderId);
        validateOrderOperation(order, userId, OrderOperationType.CANCEL);

        // 1. 更新订单状态
        order.setStatus(OrderStatusEnum.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 2. 恢复关联商品的状为 "在售"
        List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        if (!orderItems.isEmpty()) {
            List<Integer> itemIdsToRestore = orderItems.stream().map(OrderItem::getItemId).collect(Collectors.toList());
            // 批量更新商品状态
            List<Item> itemsToUpdate = itemIdsToRestore.stream().map(id -> {
                Item item = new Item();
                item.setId(id);
                item.setStatus("在售"); // 恢复为在售
                return item;
            }).collect(Collectors.toList());
            itemService.updateBatchById(itemsToUpdate);
        }

        return order;
    }

    @Override
    public IPage<Order> findOrders(Page<Order> page, OrderQueryParam queryParam) {
        // 调用 Mapper 的分页查询，需要在 XML 中实现具体逻辑
        IPage<Order> orderPage = orderMapper.findOrderPage(page, queryParam);
        // 查询关联信息 (OrderItem, Address, Buyer/Seller Nickname) 并填充到 Order 对象中
        if (orderPage != null && !CollectionUtils.isEmpty(orderPage.getRecords())) {
            for (Order order : orderPage.getRecords()) {
                // 查询订单项
                order.setOrderItems(orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())));
                // 查询地址 (如果 addressId 存在)
                if (order.getAddressId() != null) {
                    order.setAddress(addressMapper.selectById(order.getAddressId()));
                }
                // 查询买卖家昵称 (按需)
                 if (order.getBuyerId() != null) {
                     User buyer = userMapper.selectById(order.getBuyerId());
                     if(buyer != null) order.setBuyerNickname(buyer.getNickname());
                 }
                 if (order.getSellerId() != null) {
                      User seller = userMapper.selectById(order.getSellerId());
                      if(seller != null) order.setSellerNickname(seller.getNickname());
                 }
            }
        }
        return orderPage;
    }

    @Override
    public Order getOrderDetail(Integer userId, Integer orderId) throws Exception {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new Exception("订单不存在");
        }
        // 权限校验：必须是买家或卖家才能查看
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new Exception("无权查看此订单");
        }

        // 查询并填充关联信息
        order.setOrderItems(orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())));
        if (order.getAddressId() != null) {
            order.setAddress(addressMapper.selectById(order.getAddressId()));
        }
         if (order.getBuyerId() != null) {
             User buyer = userMapper.selectById(order.getBuyerId());
             if(buyer != null) order.setBuyerNickname(buyer.getNickname());
         }
         if (order.getSellerId() != null) {
              User seller = userMapper.selectById(order.getSellerId());
              if(seller != null) order.setSellerNickname(seller.getNickname());
         }

        return order;
    }


    // --- 辅助方法 ---

    private enum OrderOperationType { SET_ADDRESS, PAY, SHIP, CONFIRM_RECEIPT, CANCEL }

    /**
     * 校验订单操作的通用逻辑
     * @param order 订单对象
     * @param userId 操作用户ID
     * @param operationType 操作类型
     * @throws Exception 校验失败则抛出异常
     */
    private void validateOrderOperation(Order order, Integer userId, OrderOperationType operationType) throws Exception {
        if (order == null) {
            throw new Exception("订单不存在");
        }

        switch (operationType) {
            case SET_ADDRESS:
            case PAY:
            case CONFIRM_RECEIPT:
            case CANCEL: // 这些操作必须是买家本人
                if (!order.getBuyerId().equals(userId)) {
                    throw new Exception("无权操作他人订单");
                }
                break;
            case SHIP: // 发货操作必须是卖家本人
                if (!order.getSellerId().equals(userId)) {
                    throw new Exception("无权操作他人订单");
                }
                break;
            default:
                throw new IllegalArgumentException("未知的操作类型");
        }

        // 状态校验
        OrderStatusEnum currentStatus = order.getStatus();
        switch (operationType) {
            case SET_ADDRESS:
            case PAY:
            case CANCEL: // 必须是待付款状态
                if (currentStatus != OrderStatusEnum.PENDING_PAYMENT) {
                    throw new Exception("订单当前状态无法进行此操作");
                }
                break;
            case SHIP: // 必须是待发货状态
                if (currentStatus != OrderStatusEnum.PENDING_SHIPMENT) {
                    throw new Exception("订单当前状态无法进行此操作");
                }
                break;
            case CONFIRM_RECEIPT: // 必须是待收货状态
                if (currentStatus != OrderStatusEnum.PENDING_RECEIPT) {
                    throw new Exception("订单当前状态无法进行此操作");
                }
                break;
        }
    }
}