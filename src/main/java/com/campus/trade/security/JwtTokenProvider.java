package com.campus.trade.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenProvider {

    private static final String CLAIM_KEY_USERNAME = "sub"; // subject，存放用户名(这里用学号)
    private static final String CLAIM_KEY_USERID = "uid";   // 存放用户ID
    private static final String CLAIM_KEY_CREATED = "created";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.tokenHead}")
    private String tokenHead;

    // 将配置文件中的 secret 字符串转换为 SecretKey 对象
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
     * 从 token 中获取用户 ID
     */
    public String getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get(CLAIM_KEY_USERID, String.class));
    }

    /**
     * 从 token 中获取 JWT 签发时间
     */
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    /**
     * 从 token 中获取 JWT 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从 token 中获取指定的 Claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析 token 获取所有的 Claims
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 校验 token 是否过期
     */
    private boolean isTokenExpired(String token) {
        final Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    /**
     * 根据用户信息生成 token
     * @param username 用户名 (学号)
     * @param userId 用户ID
     */
    public String generateToken(String username, String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, username);
        claims.put(CLAIM_KEY_USERID, userId);
        claims.put(CLAIM_KEY_CREATED, new Date());
        return doGenerateToken(claims);
    }

    /**
     * 生成 token 的具体逻辑
     */
    private String doGenerateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(generateExpirationDate())
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // 使用 HS512 签名算法
                .compact();
    }

    /**
     * 生成 token 过期时间
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration);
    }

    /**
     * 验证 token 是否有效
     * @param token       客户端传入的 token
     * @param userDetails 从数据库中查询出来的用户信息
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 判断 token 是否可以被刷新 (例如，快过期时)
     * (本项目暂不实现刷新逻辑，但可以预留)
     */
    public boolean canTokenBeRefreshed(String token) {
        return !isTokenExpired(token);
    }

    /**
     * 刷新 token
     * (本项目暂不实现刷新逻辑)
     */
    public String refreshToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        claims.put(CLAIM_KEY_CREATED, new Date());
        return doGenerateToken(claims);
    }

    public String getTokenHead() {
        return tokenHead;
    }
}