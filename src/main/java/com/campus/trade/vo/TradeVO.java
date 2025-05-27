package com.campus.trade.vo;
import com.campus.trade.enums.TradeTypeEnum;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class TradeVO implements Serializable { // 交易记录展示用
    private Integer id;
    private Integer orderId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private TradeTypeEnum tradeType;
    private String description;
    private LocalDateTime createdAt;
}
