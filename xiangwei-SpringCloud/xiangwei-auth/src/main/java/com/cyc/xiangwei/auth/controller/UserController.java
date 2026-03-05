package com.cyc.xiangwei.auth.controller;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.cyc.xiangwei.auth.entity.RefreshTokenRequest;
import com.cyc.xiangwei.auth.entity.User;
import com.cyc.xiangwei.auth.service.UserService;
import com.cyc.xiangwei.common.utils.Result;
import com.cyc.xiangwei.common.utils.ResultCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Integer getIntegerHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (!StringUtils.hasText(value)) { return null; }
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return null; }
    }

    @PostMapping("/login")
    public Result<?> login(@Validated @RequestBody User user) {
        // 注：Java 规范建议方法名首字母小写，如果您在 UserService 里写的是大写的 Login，这里照旧调用即可
        Map<String, Object> map = userService.Login(user.getUsername(), user.getPassword());
        if(map == null) {
            // 使用 PARAM_ERROR 返回 400，比返回 500 更符合 RESTful 规范
            return Result.error(ResultCodeEnum.PARAM_ERROR, "用户名或密码不正确");
        }
        return Result.success(map);
    }

    @PostMapping("/refresh")
    public Result<?> refresh(@Validated @RequestBody RefreshTokenRequest req) {
        Map<String, Object> refresh = userService.refresh(req);
        return Result.success(refresh);
    }

    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody User user) {
        boolean isRegistered = userService.Register(user);
        if (isRegistered) {
            return Result.success();
        }
        return Result.error(ResultCodeEnum.PARAM_ERROR, "用户名已被使用");
    }

    @GetMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String username = request.getHeader("username");
        if (StringUtils.hasText(username)) {
            userService.logout(username);
        }
        return Result.success();
    }

    @GetMapping("/getUser")
    public Result<?> getUser(@RequestParam(defaultValue = "1") int pageNum,
                             @RequestParam(defaultValue = "5") int size,
                             @RequestParam(defaultValue = "") String search,
                             HttpServletRequest request) {

        // 利用辅助方法安全获取
        Integer type = getIntegerHeader(request, "type");

        if (type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 0) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(search)) {
            wrapper.like(User::getUsername, search);
        }

        Page<User> page = userService.page(new Page<>(pageNum, size), wrapper);
        return Result.success(page);
    }

    @PutMapping("/updateUser")
    public Result<?> updateUser(@Validated @RequestBody User user, HttpServletRequest request) {
        Integer type = getIntegerHeader(request, "type");
        Integer userId = getIntegerHeader(request, "userId");

        if (type == null || userId == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }

        // 0是管理员，管理员可以改所有人；非管理员只能改自己
        if(type != 0 && !userId.equals(user.getId())){
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        userService.update(user, userId, type);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") Integer id, HttpServletRequest request) {
        Integer type = getIntegerHeader(request, "type");

        if (type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }

        // 仅管理员 (type == 0) 可以删除
        if (type == 0) {
            if (userService.removeById(id)) {
                return Result.success();
            }
            return Result.error(ResultCodeEnum.ERROR, "删除失败，数据可能不存在");
        } else {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }
    }

    @GetMapping("/internal/getUserOne/{id}")
    public Result<?> getUserOne(@PathVariable("id") Integer id) {
        User user = userService.getById(id);
        if(user != null){
            return Result.success(user);
        }
        return Result.error(ResultCodeEnum.NOT_FOUND, "没有该用户");
    }
}
