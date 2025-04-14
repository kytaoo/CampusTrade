package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.trade.enums.UserStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user") // 显式指定表名
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO) // 主键自增
    private Integer id;

    @TableField("student_id") // 明确数据库字段名
    private String studentId;

    private String password;

    private String nickname;

    private String avatar;

    private String phone;

    private String email;

    // 使用 Mybatis Plus 的枚举处理器
    @TableField("status")
    private UserStatusEnum status;

    private BigDecimal balance;

    @TableField(fill = FieldFill.INSERT) // MP 自动填充创建时间
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE) // MP 自动填充更新时间
    private LocalDateTime updatedAt;

    // @TableLogic // 如果设计了逻辑删除字段，可以取消注释
    // private Integer isDeleted;
}