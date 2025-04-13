package com.campus.trade.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置允许跨域请求
        registry.addMapping("/**") // 对所有路径生效
                .allowedOriginPatterns("*") // 允许所有来源的请求 (生产环境建议指定具体来源)
                // .allowedOrigins("http://localhost:5173") // 开发环境指定前端地址
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的方法
                .allowCredentials(true) // 允许携带凭证 (如 Cookie)
                .maxAge(3600); // 预检请求的有效期 (秒)
    }
}