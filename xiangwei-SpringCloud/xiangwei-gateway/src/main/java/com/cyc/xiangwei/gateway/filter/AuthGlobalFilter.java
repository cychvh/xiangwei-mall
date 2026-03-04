package com.cyc.xiangwei.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    // 1. 白名单 (对应您单体拦截器中的 uri.contains 放行逻辑)
    private static final List<String> WHITELIST = Arrays.asList(
            "/user/login",
            "/user/register",
            "/user/refresh",
            "/product/user/productOne/**",
            "/product/AU/list",
            "/notice/getNotice"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String SECRET_KEY = "blog-secret-key-must-be-at-least-256-bits-long-for-security";
    private static final String REFRESH_KEY_PREFIX = "token:"; // 对应您的 Redis Key 前缀

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        if (pathMatcher.match("/**/internal/**", path)) {
            return errorResponse(exchange, HttpStatus.FORBIDDEN, "403", "非法访问：内部接口不对外暴露");
        }
        // 2. 放行 OPTIONS 请求和白名单
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return chain.filter(exchange);
        }
        for (String pattern : WHITELIST) {
            if (pathMatcher.match(pattern, path)) {
                return chain.filter(exchange);
            }
        }

        // 3. 取 accessToken (对应单体拦截器第 1 步)
        String auth = request.getHeaders().getFirst("Authorization");
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange, "未登录，请提供 Bearer Token");
        }
        String accessToken = auth.substring(7).trim();

        try {
            // 修改 2：与 JWTConfig 保持完全一致的解析算法
            // 使用 Keys.hmacShaKeyFor 和 UTF_8 编码来生成验签 Key
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();

            // 修改 3：校验 Token 类型 (防止拿 Refresh Token 访问业务接口)
            String typ = claims.get("typ", String.class);
            if (!"access".equals(typ)) {
                return unauthorizedResponse(exchange, "Token 类型错误，请使用 AccessToken");
            }

            // 4. 直接从 Token 载荷中提取关键信息 (无需查 DB！)
            String username = claims.getSubject(); // 因为您 JWTConfig 中 setSubject 放的是 username
            String userId = claims.get("userId") != null ? claims.get("userId").toString() : "";
            String type = claims.get("type") != null ? claims.get("type").toString() : "";

            if (!StringUtils.hasText(username)) {
                return unauthorizedResponse(exchange, "Token 解析异常，缺少用户名");
            }

            // 5. Redis 登录态校验
            // 完美保留您的双 Token 安全机制！
            String refreshKey = REFRESH_KEY_PREFIX + username;
            Boolean hasKey = stringRedisTemplate.hasKey(refreshKey);
            if (Boolean.FALSE.equals(hasKey)) {
                return unauthorizedResponse(exchange, "登录状态已失效，请重新登录");
            }

            // 6. 设置上下文，传递给下游微服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("userId", userId)
                    .header("type", type)
                    .header("username", username)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
            return chain.filter(mutatedExchange);

        } catch (Exception e) {
            // 如果签名不对、或者 Token 过期，都会走到这里
            return unauthorizedResponse(exchange, "Token 无效或已过期，请重新登录");
        }
    }

    /**
     * 封装 401 返回格式
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String resultJson = "{\"code\":\"401\", \"msg\":\"" + msg + "\", \"data\":null}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(resultJson.getBytes())));
    }

    private Mono<Void> errorResponse(ServerWebExchange exchange, HttpStatus httpStatus, String code, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String resultJson = "{\"code\":\"" + code + "\", \"msg\":\"" + msg + "\", \"data\":null}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(resultJson.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100; // 优先级设置很高
    }
}