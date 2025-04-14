package com.campus.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserStatusEnum {
    PENDING_VERIFICATION(0, "待邮箱验证"),
    ACTIVATED(1, "已激活/正常"),
    FROZEN(2, "已冻结/禁用");

    @EnumValue // 标记数据库存的值是 code
    private final int code;
    private final String description;

    UserStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }
}