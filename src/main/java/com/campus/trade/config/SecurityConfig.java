package com.campus.trade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // 启用 Spring Security
public class SecurityConfig {

    // 配置密码编码器 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. 禁用 CSRF 防护 (因为我们使用 JWT，不需要 Session)
            .csrf().disable()

            // 2. 配置 Session 管理策略为 STATELESS (无状态)
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()

            // 3. 配置 URL 授权规则
            .authorizeRequests(authorize -> authorize
                // 对于 /hello 接口和后续的 /auth/** 接口，允许所有访问
                .antMatchers("/hello", "/auth/**").permitAll()
                // 其他所有请求都需要身份认证 (暂时注释掉，允许所有访问，方便初期测试)
                // .anyRequest().authenticated()
                .anyRequest().permitAll() // !!注意: 阶段一暂时允许所有，方便测试基础接口
            );

            // 4. 配置 CORS (Spring Security 会利用 WebMvcConfig 中定义的 CorsConfigurationSource)
            // http.cors(); // 如果 WebMvcConfig 配置了全局 CORS，这里可以省略或按需配置

            // 5. 配置 JWT 过滤器 (将在后续阶段添加)
            // http.addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);

            // 6. 配置异常处理 (将在后续阶段完善)
            // .exceptionHandling()
            // .authenticationEntryPoint(authenticationEntryPoint()) // 未认证处理器
            // .accessDeniedHandler(accessDeniedHandler());        // 未授权处理器

        return http.build();
    }

    // --- 后续阶段需要添加的 Bean ---
    // @Bean
    // public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter() { ... }
    // @Bean
    // public AuthenticationEntryPoint authenticationEntryPoint() { ... }
    // @Bean
    // public AccessDeniedHandler accessDeniedHandler() { ... }
}