package com.cyc.xiangwei.auth.controller;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.cyc.xiangwei.auth.entity.RefreshTokenRequest;
import com.cyc.xiangwei.auth.entity.User;
import com.cyc.xiangwei.auth.service.UserService;
import com.cyc.xiangwei.common.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


import java.util.Map;


@RestController
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user/login")
    public Result<?> login(@RequestBody User user) {
        Map<String, Object> map = userService.Login(user.getUsername(), user.getPassword());
        if(map == null) {
            return Result.error("500","用户名或密码不正确");
        }
        return Result.success(map);
    }

    @PostMapping("/user/refresh")
    public Result<?> refresh(@RequestBody RefreshTokenRequest req) {
        try {
            Map<String, Object> refresh = userService.refresh(req);
            return Result.success(refresh);
        } catch (Exception e) {
            return Result.error("500",e.getMessage());
        }
    }


    @PostMapping("/user/register")
    public Result<?> Register(@RequestBody User user) {
        boolean register = userService.Register(user);
        if (register) {
            return Result.success();
        }
        return Result.error("10086","用户名已被使用");
    }


    @GetMapping("/user/logout")
    public Result<?> logout(HttpServletRequest request) {
        String username =  request.getHeader("username");
        userService.logout(username);
        return Result.success();
    }


    @GetMapping("/user/getUser")
    public Result<?> getUser(@RequestParam(defaultValue = "1") int pageNum,
                             @RequestParam(defaultValue = "5") int size,
                             @RequestParam(defaultValue = "") String search,
                             HttpServletRequest request) {
        String typeStr = request.getHeader("type");
        if (typeStr == null || "".equals(typeStr)) {
            return Result.error("401", "登录状态异常，缺少权限信息");
        }
        Integer type = Integer.parseInt(typeStr);
        if (!type.equals(0)) {
            return Result.error("405","权限不足");
        }else {
            LambdaQueryWrapper<User>lambdaQueryWrapper = new LambdaQueryWrapper<>();
            if(StringUtils.hasText(search)){
                lambdaQueryWrapper.like(User::getUsername,search);
            }
            System.out.println(size);
            Page<User> page = userService.page(new Page<>(pageNum, size), lambdaQueryWrapper);
            return Result.success(page);
        }
    }

    @PutMapping("/user/updateUser")
    public Result<?>  updateUser(@RequestBody User user,HttpServletRequest request) {
        String typeStr = request.getHeader("type");
        String userIdStr = request.getHeader("userId");
        if (typeStr == null || userIdStr == null) {
            return Result.error("401", "未登录或登录状态失效");
        }
        Integer type = Integer.parseInt(typeStr);
        Integer userId = Integer.parseInt(userIdStr);
        if(!type.equals(0) && !userId.equals(user.getId())){
            return Result.error("405","权限不够");
        }
        userService.update(user,userId,type);
        return Result.success();
    }



    @DeleteMapping("/user/{id}")
    public Result<?> delete(@PathVariable("id") Integer id, HttpServletRequest request) {
        String typeStr = request.getHeader("type");

        // 1. 安全校验：如果根本没有传权限头，直接拦截
        if (typeStr == null || typeStr.trim().isEmpty()) {
            return Result.error("401", "未登录或缺少权限信息");
        }

        // 2. 权限校验：把常量 "0" 写在前面，这是 Java 防空指针的经典小技巧！
        // 即使 typeStr 碰巧是 null， "0".equals(null) 也只是返回 false，绝对不会报错！
        if ("0".equals(typeStr)) {
            if (userService.removeById(id)) {
                return Result.success();
            }
            return Result.error("500", "删除失败");
        } else {
            return Result.error("405", "权限不足，仅管理员可删除");
        }
    }

    @GetMapping("/user/internal/getUserOne/{id}")
    public Result<?> getUserOne(@PathVariable("id") Integer id,HttpServletRequest request) {
            User user = userService.getById(id);
            if(user != null){
                return Result.success(user);
            }
            return Result.error("500","没有该用户");
    }
}
