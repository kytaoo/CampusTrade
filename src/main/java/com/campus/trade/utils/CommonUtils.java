package com.campus.trade.utils;

import java.util.Random;

public class CommonUtils {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();

    /**
     * 生成指定长度的随机字符串 (大小写字母+数字)
     * @param length 长度
     * @return 随机字符串
     */
    public static String generateRandomCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive.");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}