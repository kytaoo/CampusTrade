package com.campus.trade.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Trade;
import com.campus.trade.enums.TradeTypeEnum;
import java.math.BigDecimal;

public interface ITradeService extends IService<Trade> {
    /**
     * 记录交易流水
     * @param userId 用户ID
     * @param orderId 关联订单ID (可选)
     * @param amount 交易金额 (正/负)
     * @param balanceAfter 交易后余额
     * @param tradeType 交易类型
     * @param description 描述
     */
    void recordTrade(Integer userId, Integer orderId, BigDecimal amount, BigDecimal balanceAfter, TradeTypeEnum tradeType, String description);

    /**
     * 分页查询用户交易记录
     * @param page 分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<Trade> findUserTrades(Page<Trade> page, Integer userId);
}