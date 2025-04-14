package com.campus.trade.constant;

public class RedisKeys {

    /**
     * 邮箱验证码 Key 前缀
     * 完整 Key 示例: verify_code:email:user@example.com
     */
    public static final String VERIFY_CODE_EMAIL_PREFIX = "verify_code:email:";

    /**
     * JWT 黑名单 Key 前缀 (用于登出，可选实现)
     * 完整 Key 示例: jwt:blacklist:xxxxxxxxxxxxx
     */
    public static final String JWT_BLACKLIST_PREFIX = "jwt:blacklist:";

    // 可以根据需要添加其他 Key
}