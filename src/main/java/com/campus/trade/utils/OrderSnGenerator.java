package com.campus.trade.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class OrderSnGenerator {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /**
     * 生成唯一的订单号 (时间戳 + 随机数)
     * @return 订单号字符串
     */
    public static String generateOrderSn() {
        String timestamp = DATE_FORMAT.format(new Date());
        // 生成 4 位随机数
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
        return timestamp + randomNum;
    }
}