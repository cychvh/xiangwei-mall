package com.cyc.xiangwei.auth.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.cyc.xiangwei.auth.dao.UserMapper;
import com.cyc.xiangwei.auth.entity.RefreshTokenRequest;
import com.cyc.xiangwei.auth.entity.User;
import com.cyc.xiangwei.auth.service.UserService;
import com.cyc.xiangwei.auth.config.JWTConfig;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    private final JWTConfig jwtConfig;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, RedisTemplate<String, Object> redisTemplate, JWTConfig jwtConfig, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.jwtConfig = jwtConfig;
        this.passwordEncoder = passwordEncoder;
    }

    private static final String REFRESH_KEY_PREFIX = "token:";
    private static final String USER_CACHE_PREFIX = "login:user:";

    @Override
    public Map<String, Object> Login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User formUser = userMapper.selectOne(queryWrapper);

        if (formUser == null || !passwordEncoder.matches(password, formUser.getPassword())) {
            return null;
        }

        formUser.setPassword(null);

        String accessToken = jwtConfig.generateAccessToken(formUser.getUsername(),formUser.getId(),formUser.getType());
        String refreshToken = jwtConfig.generateRefreshToken(formUser.getUsername());

        // refresh token 存 Redis（7天）
        String refreshKey = REFRESH_KEY_PREFIX + formUser.getUsername();
        redisTemplate.opsForValue().set(refreshKey, refreshToken, jwtConfig.getRefreshExpireMs(), TimeUnit.MILLISECONDS);
        Map<String, Object> map = new HashMap<>();
        map.put("accessToken", accessToken);
        map.put("refreshToken", refreshToken);
        map.put("user", formUser);
        return map;
    }

    @Override
    public boolean Register(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User user1 = userMapper.selectOne(queryWrapper);
        if (user1 != null) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.insert(user) > 0;
    }

    @Override
    public Map<String, Object> refresh(RefreshTokenRequest req) {
        String refreshToken = req.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("refreshToken不能为空");
        }

        // 1) refreshToken 必须合法且 typ=refresh
        if (!jwtConfig.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("refreshToken无效或已过期");
        }

        // 2) 取 username
        String username = jwtConfig.getUsernameFromToken(refreshToken);

        // 3) 必须与 Redis 中保存的 refreshToken 一致（关键！防止伪造/旧token继续用）
        String refreshKey = REFRESH_KEY_PREFIX + username;
        Object saved = redisTemplate.opsForValue().get(refreshKey);
        if (saved == null) {
            throw new RuntimeException("登录状态已失效，请重新登录");
        }
        String savedRefreshToken = saved.toString();
        if (!savedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("refreshToken不匹配，请重新登录");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User dbUser = userMapper.selectOne(queryWrapper);

        if (dbUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 4) 生成新的 accessToken
        String newAccessToken = jwtConfig.generateAccessToken(dbUser.getUsername(), dbUser.getId(), dbUser.getType());
        String newRefreshToken = jwtConfig.generateRefreshToken(dbUser.getUsername());
        redisTemplate.opsForValue().set(refreshKey, newRefreshToken, jwtConfig.getRefreshExpireMs(), TimeUnit.MILLISECONDS);

        Map<String, Object> map = new HashMap<>();
        map.put("accessToken", newAccessToken);
        map.put("refreshToken", newRefreshToken);
        return map;
    }

    @Override
    public void logout(String username) {
        if (username != null && !username.isBlank()) {
            redisTemplate.delete(REFRESH_KEY_PREFIX + username);
            redisTemplate.delete(USER_CACHE_PREFIX + username);
        }
    }

    @Override
    public void update(User user, Integer userId, Integer type) {
        User updateUser = new User();
        updateUser.setId(user.getId());

        boolean isCriticalUpdate = false; // 🚀 标记是否发生了高危修改（如改密码、改权限）

        if (type == 0) {
            // 【管理员】：可以修改其他人的用户名和角色类型
            if (StringUtils.hasText(user.getUsername())) {
                updateUser.setUsername(user.getUsername());
                isCriticalUpdate = true; // 修改用户名会导致 Token 失效
            }
            if (user.getType() != null) {
                updateUser.setType(user.getType());
                isCriticalUpdate = true; // 修改权限必须强制下线重新签发 Token
            }
        } else {
            // 【普通用户/商家】：不能改角色
            // 1. 修改密码
            if (StringUtils.hasText(user.getPassword())) {
                updateUser.setPassword(passwordEncoder.encode(user.getPassword()));
                isCriticalUpdate = true; // 自己改了密码，必须强制下线
            }
            if(StringUtils.hasText(user.getAddress())) {
                updateUser.setAddress(user.getAddress());
            }


        }

        baseMapper.updateById(updateUser);

        // 🚀 核心新增：数据一致性保护 - 强制下线机制
        if (isCriticalUpdate) {
            // 查出被修改的那个用户的最新 username (如果是管理员改了别人的权限，需要踢掉别人)
            User targetUser = baseMapper.selectById(updateUser.getId());
            if (targetUser != null && StringUtils.hasText(targetUser.getUsername())) {
                // 删除 Redis 里的 Token，网关的 hasKey 校验会立刻失败，强制用户重新登录
                String redisKey = REFRESH_KEY_PREFIX + targetUser.getUsername();
                redisTemplate.delete(redisKey);
                redisTemplate.delete(USER_CACHE_PREFIX + targetUser.getUsername()); // 顺手清掉缓存
            }
        }
    }

    @Override
    public int GetType(String Username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", Username);
        User user = userMapper.selectOne(queryWrapper);
        return user.getType();
    }
}
