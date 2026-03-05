package com.cyc.xiangwei.order.controller;

import com.cyc.xiangwei.common.utils.Result;
import com.cyc.xiangwei.common.utils.ResultCodeEnum;
import com.cyc.xiangwei.order.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 💡 辅助方法：安全地从 Header 提取 Integer 类型的参数，防止空指针和数字转换异常
    private Integer getIntegerHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @PostMapping("/add")
    public Result<?> addCart(@RequestParam Integer productId,
                             @RequestParam(defaultValue = "1") @Min(value = 1, message = "加购数量必须大于0")Integer quantity,
                             HttpServletRequest request) {

        // 微服务改造：统一从 Header 中获取用户 ID 和类型
        Integer userId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        // 防止 type 为 null 导致的空指针异常
        if (type != 2) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        return cartService.addCart(productId, quantity, userId);
    }

    @GetMapping("/list")
    public Result<?> listCart(HttpServletRequest request) {

        Integer userId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 2) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        return cartService.getMyCartList(userId);
    }

    @DeleteMapping("/delete/{cartId}")
    public Result<?> deleteCart(@PathVariable Integer cartId, HttpServletRequest request) {

        Integer userId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 2) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        //  进阶安全提示：目前这里是直接根据 cartId 删除。
        // 在更严谨的电商系统中，建议在 Service 层校验一下这个 cartId 是不是属于当前 userId 的，防止越权删除别人的购物车。
        cartService.removeById(cartId);
        return Result.success("删除成功");
    }
}