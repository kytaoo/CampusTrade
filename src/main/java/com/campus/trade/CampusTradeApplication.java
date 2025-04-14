package com.campus.trade;

import org.mybatis.spring.annotation.MapperScan; // 确保有这个，如果 MybatisPlusConfig 里没有 @MapperScan
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // 导入并启用异步

@SpringBootApplication
@EnableAsync // 启用异步方法执行 (@Async 注解生效)
// @MapperScan("com.campus.trade.mapper") // 如果 MybatisPlusConfig 里没有，则需要在这里指定
public class CampusTradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusTradeApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  校园二手交易平台后端启动成功 (阶段二完成)   ლ(´ڡ`ლ)ﾞ");
    }
}