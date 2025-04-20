package com.campus.trade.dto;

import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty; // 注意区分 @NotEmpty 和 @NotBlank
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemPublishReqDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "商品标题不能为空")
    private String title;

    private String description; // 描述可以为空

    @NotBlank(message = "商品分类不能为空")
    private String category;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于等于 0.01")
    private BigDecimal price;

    @NotBlank(message = "新旧程度不能为空")
    private String condition;

    // 图片 URL 列表在 Controller 层通过 @RequestPart 处理，不在 DTO 中直接接收文件
    // 如果前端选择传递 base64 编码的图片，可以在这里定义 List<String> imagesBase64;
}