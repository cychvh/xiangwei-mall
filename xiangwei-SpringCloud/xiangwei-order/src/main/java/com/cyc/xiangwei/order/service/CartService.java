package com.cyc.xiangwei.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyc.xiangwei.common.utils.Result;
import com.cyc.xiangwei.order.entity.Cart;

public interface CartService extends IService<Cart> {
    Result<?> addCart(Integer productId, Integer quantity, Integer userId);
    Result<?> getMyCartList(Integer userId);
}