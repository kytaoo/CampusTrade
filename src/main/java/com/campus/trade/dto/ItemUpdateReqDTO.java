package com.campus.trade.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ItemUpdateReqDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "商品ID不能为空")
    private Integer id; // 更新时必须携带 ID

    @NotBlank(message = "商品标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "商品分类不能为空")
    private String category;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于等于 0.01")
    private BigDecimal price;

    @NotBlank(message = "新旧程度不能为空")
    private String condition;

    // 图片同样不在 DTO 中接收文件
}