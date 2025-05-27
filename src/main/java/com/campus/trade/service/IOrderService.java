package com.campus.trade.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.dto.OrderQueryParam; // 需要创建这个 DTO
import com.campus.trade.entity.Order;
import java.util.List;
import java.util.Map;

public interface IOrderService extends IService<Order> {

    /**
     * 从购物车创建订单 (按卖家拆分)
     * @param userId 买家ID
     * @param cartItemIds 选中的购物车项ID列表
     * @param remarkMap (可选) 按卖家ID分的订单备注 {sellerId: "remark"}
     * @return 创建成功的订单列表 (状态为待付款)
     * @throws Exception 创建失败，如商品状态变更、库存不足(暂不考虑)等
     */
    List<Order> createOrdersFromCart(Integer userId, List<Integer> cartItemIds, Map<Integer, String> remarkMap) throws Exception;

    /**
     * 直接购买创建订单
     * @param userId 买家ID
     * @param itemId 商品ID
     * @param remark 订单备注 (可选)
     * @return 创建成功的订单 (状态为待付款)
     * @throws Exception 创建失败
     */
    Order createOrderFromBuyNow(Integer userId, Integer itemId, String remark) throws Exception;

    /**
     * 买家为订单设置收货地址
     * @param userId 买家ID
     * @param orderId 订单ID
     * @param addressId 收货地址ID
     * @return 更新后的订单
     * @throws Exception 订单不存在、状态不符、非本人订单、地址不存在等
     */
    Order setOrderAddress(Integer userId, Integer orderId, Integer addressId) throws Exception;

    /**
     * 支付订单 (余额支付)
     * @param userId 买家ID
     * @param orderIds 需要支付的订单ID列表 (允许合并支付)
     * @return 支付成功的订单列表 (如果部分失败怎么处理？这里假设要么全成功要么全失败)
     * @throws Exception 余额不足、订单状态错误、支付失败等
     */
    List<Order> payOrders(Integer userId, List<Integer> orderIds) throws Exception;

    /**
     * 卖家发货
     * @param userId 卖家ID
     * @param orderId 订单ID
     * @return 更新后的订单
     * @throws Exception 订单不存在、状态不符、非本人订单等
     */
    Order shipOrder(Integer userId, Integer orderId) throws Exception;

    /**
     * 买家确认收货
     * @param userId 买家ID
     * @param orderId 订单ID
     * @return 更新后的订单
     * @throws Exception 订单不存在、状态不符、非本人订单等
     */
    Order confirmReceipt(Integer userId, Integer orderId) throws Exception;

    /**
     * 买家取消订单 (仅限待付款状态)
     * @param userId 买家ID
     * @param orderId 订单ID
     * @return 更新后的订单
     * @throws Exception 订单不存在、状态不符、非本人订单等
     */
    Order cancelOrder(Integer userId, Integer orderId) throws Exception;

    /**
     * 分页查询订单列表
     * @param page 分页对象
     * @param queryParam 查询参数 (包含用户ID、视角[买家/卖家]、状态等)
     * @return 分页结果
     */
    IPage<Order> findOrders(Page<Order> page, OrderQueryParam queryParam);

    /**
     * 获取订单详情 (包含商品项和地址信息)
     * @param userId 当前用户ID (用于权限校验)
     * @param orderId 订单ID
     * @return 订单详情
     * @throws Exception 订单不存在或无权查看
     */
    Order getOrderDetail(Integer userId, Integer orderId) throws Exception;

}