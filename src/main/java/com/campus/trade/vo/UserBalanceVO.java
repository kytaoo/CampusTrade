package com.campus.trade.vo;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
@Data
public class UserBalanceVO implements Serializable {
    private BigDecimal balance;
}