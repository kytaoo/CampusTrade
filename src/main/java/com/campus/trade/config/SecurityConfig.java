package com.campus.trade.config;

import com.campus.trade.security.AccessDeniedHandlerImpl;
import com.campus.trade.security.AuthenticationEntryPointImpl;
import com.campus.trade.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod; // 确保导入 HttpMethod

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // 启用方法级别的权限注解 @PreAuthorize
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter; // 注入 JWT 过滤器

    @Autowired
    private AuthenticationEntryPointImpl authenticationEntryPoint; // 注入未认证处理器

    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler; // 注入未授权处理器

    // 获取 AuthenticationManager (原 WebSecurityConfigurerAdapter#configure(AuthenticationManagerBuilder) 替代方案)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF
                .csrf().disable()

                // 2. 配置 Session 管理策略为 STATELESS
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // 3. 配置 URL 授权规则
                .authorizeRequests(authorize -> authorize
                        // -- 放行规则 --
                        .antMatchers("/hello").permitAll() // 测试接口
                        .antMatchers("/auth/**").permitAll() // 认证相关接口 (注册、登录、验证码)
                        // 商品浏览接口 (GET 请求) 通常允许匿名访问
                        .antMatchers(HttpMethod.GET, "/items", "/items/{itemId}").permitAll()
                        .antMatchers("/images/**").permitAll() // <<-- 【确认或添加这一行】明确放行图片访问路径
                        // 文件上传接口 (如果单独提供) 可能需要认证
                        // .antMatchers("/upload/**").permitAll() // 或者 .authenticated()

                        // -- 其他所有请求都需要身份认证 --
                        .anyRequest().authenticated()
                )

                // 4. 将 JWT 过滤器添加到 UsernamePasswordAuthenticationFilter 之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 5. 配置异常处理
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint) // 处理未认证
                .accessDeniedHandler(accessDeniedHandler);        // 处理未授权

        // 6. 启用 CORS (利用 WebMvcConfig 中的配置)
        http.cors(); // 确保应用 WebMvcConfig 中的 CORS 设置

        return http.build();
    }
}