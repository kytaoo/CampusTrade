package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
// import com.campus.trade.enums.ItemStatusEnum; // 如果定义了枚举
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "item", autoResultMap = true) // autoResultMap = true 用于 JSON 类型处理器
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId; // 发布者用户ID (卖家)

    private String title; // 商品标题

    private String description; // 商品描述

    private String category; // 分类

    private BigDecimal price; // 价格

    // 使用 Mybatis Plus 的 Jackson 类型处理器来处理 JSON 字段
    @TableField(value = "images", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> images; // 商品图片URL列表 (存储相对路径, 例如 /images/xxx.jpg)

    @TableField("`condition`") // condition 是 SQL 关键字，建议加反引号
    private String condition; // 新旧程度

    // 可以定义一个枚举类 ItemStatusEnum
    // @TableField("status")
    // private ItemStatusEnum status;
    // 或者直接用字符串
    @TableField("status")
    private String status; // 商品状态: 在售 (ON_SALE), 已售 (SOLD), 下架 (OFF_SHELF)

    @TableField("click_count")
    private Integer clickCount; // 点击量

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 发布时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt; // 最后更新时间

    // --- 非数据库字段，用于关联查询显示卖家信息 ---
    @TableField(exist = false)
    private String sellerNickname;
    @TableField(exist = false)
    private String sellerAvatar;
}