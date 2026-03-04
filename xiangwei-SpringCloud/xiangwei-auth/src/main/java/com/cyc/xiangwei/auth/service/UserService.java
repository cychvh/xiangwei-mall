package com.cyc.xiangwei.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyc.xiangwei.auth.entity.RefreshTokenRequest;
import com.cyc.xiangwei.auth.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> Login(String username, String password);
    Map<String, Object> refresh (RefreshTokenRequest req);
    void logout(String username);
    void update(User user,Integer userId,Integer type);
    boolean Register(User user);
    int GetType (String Username);

}
