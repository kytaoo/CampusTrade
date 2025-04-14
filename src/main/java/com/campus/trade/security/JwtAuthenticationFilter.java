package com.campus.trade.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 登录授权过滤器
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService; // UserDetailsServiceImpl

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 从请求头获取 Authorization Header
        String authHeader = request.getHeader(this.tokenHeader);

        // 2. 检查 Header 是否存在且以指定的 Token 前缀开头
        if (authHeader != null && authHeader.startsWith(this.tokenHead)) {
            // 3. 提取 JWT (去掉前缀)
            final String authToken = authHeader.substring(this.tokenHead.length());
            // 4. 从 Token 中解析出用户名 (学号)
            String username = jwtTokenProvider.getUsernameFromToken(authToken);
            log.debug("JWT Filter: Checking authentication for user '{}'", username);

            // 5. 当 Token 中的用户名不为空，且 SecurityContext 中没有认证信息时，进行认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 6. 根据用户名加载 UserDetails
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                // 7. 验证 Token 是否有效 (根据 UserDetails)
                if (jwtTokenProvider.validateToken(authToken, userDetails)) {
                    // 8. 创建认证通过的 Authentication 对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // 9. 将认证信息设置到 SecurityContext 中
                    log.debug("JWT Filter: Authenticated user '{}', setting security context", username);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.warn("JWT Filter: Invalid JWT token for user '{}'", username);
                }
            } else {
                 log.debug("JWT Filter: Username is null or SecurityContext already has Authentication");
            }
        } else {
             log.trace("JWT Filter: Could not find bearer string, will ignore the header");
        }

        // 无论是否认证成功，都放行请求，让后续过滤器处理
        filterChain.doFilter(request, response);
    }
}