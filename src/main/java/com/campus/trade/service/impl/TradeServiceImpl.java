package com.campus.trade.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.entity.Trade;
import com.campus.trade.enums.TradeTypeEnum;
import com.campus.trade.mapper.TradeMapper;
import com.campus.trade.service.ITradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class TradeServiceImpl extends ServiceImpl<TradeMapper, Trade> implements ITradeService {

    @Autowired
    private TradeMapper tradeMapper;

    @Override
    public void recordTrade(Integer userId, Integer orderId, BigDecimal amount, BigDecimal balanceAfter, TradeTypeEnum tradeType, String description) {
        Trade trade = new Trade();
        trade.setUserId(userId);
        trade.setOrderId(orderId);
        trade.setAmount(amount);
        trade.setBalanceAfter(balanceAfter);
        trade.setTradeType(tradeType);
        trade.setDescription(description);
        // createdAt 由 MP 自动填充
        tradeMapper.insert(trade);
    }

    @Override
    public IPage<Trade> findUserTrades(Page<Trade> page, Integer userId) {
        return tradeMapper.selectPage(page, new LambdaQueryWrapper<Trade>()
                .eq(Trade::getUserId, userId)
                .orderByDesc(Trade::getCreatedAt));
    }
}