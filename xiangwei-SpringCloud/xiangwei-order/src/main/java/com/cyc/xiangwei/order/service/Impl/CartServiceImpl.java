package com.cyc.xiangwei.order.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyc.xiangwei.common.utils.Result;
import com.cyc.xiangwei.order.dao.CartMapper;
import com.cyc.xiangwei.order.entity.Cart;
import com.cyc.xiangwei.order.entity.vo.CartVO;
import com.cyc.xiangwei.order.feign.ProductFeignClient;
import com.cyc.xiangwei.order.service.CartService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final ProductFeignClient productFeignClient;

    public CartServiceImpl(ProductFeignClient productFeignClient) {
        this.productFeignClient = productFeignClient;
    }

    @Override
    public Result<?> addCart(Integer productId, Integer quantity, Integer userId) {
        // 先检查购物车是否已经有这个商品
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId).eq(Cart::getProductId, productId);
        Cart existCart = baseMapper.selectOne(queryWrapper);

        if (existCart != null) {
            // 已经存在，增加数量
            existCart.setQuantity(existCart.getQuantity() + quantity);
            baseMapper.updateById(existCart);
        } else {
            // 不存在，新增记录
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setCreateTime(new Date());
            baseMapper.insert(cart);
        }
        return Result.success("加入购物车成功");
    }

    @Override
    public Result<?> getMyCartList(Integer userId) {
        // 1. 从本地数据库查出基础购物车信息
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId).orderByDesc(Cart::getCreateTime);
        List<Cart> cartList = baseMapper.selectList(queryWrapper);

        List<CartVO> cartVOList = new ArrayList<>();

        // 2. 循环购物车，通过 Feign 调用商品微服务获取名称和图片
        for (Cart cart : cartList) {
            CartVO vo = new CartVO();
            BeanUtils.copyProperties(cart, vo);
            Result<?> productResult = productFeignClient.getProductById(cart.getProductId());
            if (productResult != null && "200".equals(productResult.getCode()) && productResult.getData() != null) {
                Map<String, Object> productData = (Map<String, Object>) productResult.getData();
                vo.setProductName((String) productData.get("name"));
                vo.setProductImage((String) productData.get("imageurl"));
                vo.setProductPrice(new BigDecimal(productData.get("price").toString()));
            } else {
                vo.setProductName("商品已失效");
            }
            cartVOList.add(vo);
        }

        return Result.success(cartVOList);
    }
}