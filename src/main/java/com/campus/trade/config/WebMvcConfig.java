// src/main/java/com/campus/trade/config/WebMvcConfig.java
package com.campus.trade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 注入商品图片配置
    @Value("${file.access.item-path-pattern}")
    private String itemAccessPathPattern;
    @Value("${file.upload.item-base-path}")
    private String itemUploadBasePath;

    // 注入头像图片配置
    @Value("${file.access.avatar-path-pattern}")
    private String avatarAccessPathPattern;
    @Value("${file.upload.avatar-base-path}")
    private String avatarUploadBasePath;


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // CORS 配置保持不变
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // --- 添加商品图片映射 ---
        String itemLocation = formatLocationPath(itemUploadBasePath);
        registry.addResourceHandler(itemAccessPathPattern) // 例如 /images/item/**
                .addResourceLocations(itemLocation);    // 例如 file:D:/Study/Code/CampusTrade/images/item/

        // --- 添加头像图片映射 ---
        String avatarLocation = formatLocationPath(avatarUploadBasePath);
        registry.addResourceHandler(avatarAccessPathPattern) // 例如 /images/avatar/**
                .addResourceLocations(avatarLocation);    // 例如 file:D:/Study/Code/CampusTrade/images/avatar/
    }

    /**
     * 格式化物理路径，确保以 "file:" 开头并以 "/" 结尾
     * @param physicalPath 物理路径
     * @return 格式化后的路径
     */
    private String formatLocationPath(String physicalPath) {
        if (physicalPath == null) return "file:/"; // 或者抛异常
        String location = "file:" + physicalPath.replace("\\", "/");
        if (!location.endsWith("/")) {
            location += "/";
        }
        return location;
    }
}