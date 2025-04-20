package com.campus.trade.config;

import org.springframework.beans.factory.annotation.Value; // 导入 Value
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry; // 导入 ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 从配置文件注入图片访问的 URL 路径模式和物理存储路径
    @Value("${file.access.path-pattern}")
    private String accessPathPattern;

    @Value("${file.upload.base-path}")
    private String uploadBasePath;

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

    /**
     * 添加静态资源处理器，用于访问上传的图片
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保物理路径以 "file:" 开头，并且根据操作系统适配路径分隔符
        String location = "file:" + uploadBasePath.replace("\\", "/");
        // 如果路径不是以 '/' 结尾，则添加 '/'
        if (!location.endsWith("/")) {
            location += "/";
        }

        registry.addResourceHandler(accessPathPattern) // 前端访问的 URL 路径模式 (例如 /images/**)
                .addResourceLocations(location); // 映射到实际的物理存储路径 (例如 file:D:/campus-trade/uploads/images/)
    }
}