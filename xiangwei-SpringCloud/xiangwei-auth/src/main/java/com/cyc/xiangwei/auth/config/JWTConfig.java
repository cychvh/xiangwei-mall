package com.cyc.xiangwei.auth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Configuration
public class JWTConfig {

    // 至少 32 字节（256 bits）以上
    private final String secret = "blog-secret-key-must-be-at-least-256-bits-long-for-security";

    // 5分钟（毫秒）
    private static final long ACCESS_EXPIRE_MS = 5 * 60 * 1000L;

    // 7天（毫秒）
    private static final long REFRESH_EXPIRE_MS = 7L * 24 * 60 * 60 * 1000;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username, Integer userId, Integer type) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .claim("typ", "access")
                .claim("username", username) // 显式存入 username 方便网关获取
                .claim("userId", userId)     // 存入用户 ID
                .claim("type", type)         // 存入权限标识

                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ACCESS_EXPIRE_MS))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .claim("typ", "refresh")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + REFRESH_EXPIRE_MS))
                .signWith(getSigningKey())
                .compact();
    }
    //从token中获取用户名
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }
    //验证accessToken的有效性
    public boolean validateAccessToken(String token) {
        try {
            Claims c = parseClaims(token);
            return "access".equals(c.get("typ", String.class));
        } catch (Exception e) {
            return false;
        }
    }
    //验证RefreshToken的有效性
    public boolean validateRefreshToken(String token) {
        try {
            Claims c = parseClaims(token);
            return "refresh".equals(c.get("typ", String.class));
        } catch (Exception e) {
            return false;
        }
    }
    //判断token有效性并取出信息部分
    private Claims parseClaims(String token) {
        //
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getAccessExpireMs() {
        return ACCESS_EXPIRE_MS;
    }

    public long getRefreshExpireMs() {
        return REFRESH_EXPIRE_MS;
    }
}
