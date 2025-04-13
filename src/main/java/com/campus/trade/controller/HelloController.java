package com.campus.trade.controller;

import com.campus.trade.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于测试项目是否成功启动和前后端是否连通的简单 Controller
 */
@RestController
public class HelloController {

    /**
     * 一个简单的 GET 请求接口，用于测试
     * @return 包含成功消息的 Result 对象
     */
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("校园二手交易平台后端已成功启动!");
    }
}