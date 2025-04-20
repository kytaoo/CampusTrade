// src/main/java/com/campus/trade/security/JwtTokenProvider.java
package com.campus.trade.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails; // 保持这个 import 用于 validateToken
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenProvider {

    // Claim keys
    private static final String CLAIM_KEY_USERNAME = "sub";    // Standard claim for subject (we use studentId)
    private static final String CLAIM_KEY_USERID = "uid";    // 【新增】Custom claim for user ID
    private static final String CLAIM_KEY_CREATED = "created"; // Standard claim for creation date

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.tokenHead}")
    private String tokenHead;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 从 token 中获取用户名 (学号)
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 【新增】从 token 中获取用户 ID
     * @param token JWT Token
     * @return 用户 ID (String 类型)，如果解析失败或不存在则返回 null
     */
    public String getUserIdFromToken(String token) {
        try {
            // 使用 getOrDefault 或直接 get 并处理可能为 null 的情况
            return getClaimFromToken(token, claims -> claims.get(CLAIM_KEY_USERID, String.class));
        } catch (Exception e) {
            log.warn("无法从 token 中获取用户 ID: {}", e.getMessage());
            return null;
        }
    }

    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // Token 解析失败 (可能过期、签名无效等)
            log.warn("解析 JWT 失败: {}", e.getMessage());
            // 可以返回一个空的 Claims 对象或者抛出异常，取决于你的错误处理策略
            // 这里返回 null，让调用者处理
            return null;
            // 或者 return Jwts.claims(); // 返回空 Claims
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            final Date expirationDate = getExpirationDateFromToken(token);
            return expirationDate != null && expirationDate.before(new Date());
        } catch (Exception e) {
            // 如果解析过期时间出错（例如 token 无效），也认为它已过期或无效
            return true;
        }
    }

    /**
     * 【修改】根据用户信息生成 token
     * @param username 用户名 (学号)
     * @param userId   用户 ID (String 类型，因为 ID 可能是数字也可能是其他格式，统一用 String)
     */
    public String generateToken(String username, String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, username);
        claims.put(CLAIM_KEY_USERID, userId); // 【修改】添加 userId 到 Claims
        claims.put(CLAIM_KEY_CREATED, new Date());
        return doGenerateToken(claims);
    }

    private String doGenerateToken(Map<String, Object> claims) {
        // ... (保持不变) ...
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(generateExpirationDate())
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration);
    }

    /**
     * 【修改】验证 token 是否有效
     * 仅校验用户名和过期时间，因为 User ID 已经在 token 中
     * @param token       客户端传入的 token
     * @param userDetails 从数据库中查询出来的用户信息 (用于比对 username)
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            // 检查用户名是否匹配，并且 Token 未过期
            return (userDetails != null && username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            // 任何解析或验证异常都视为无效
            return false;
        }
    }

    public boolean canTokenBeRefreshed(String token) {
        return !isTokenExpired(token);
    }

    public String refreshToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        if (claims == null) return null; // 解析失败无法刷新
        claims.put(CLAIM_KEY_CREATED, new Date());
        return doGenerateToken(claims);
    }

    public String getTokenHead() {
        return tokenHead;
    }
}