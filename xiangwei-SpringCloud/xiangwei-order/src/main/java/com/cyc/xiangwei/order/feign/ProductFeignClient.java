package com.cyc.xiangwei.order.feign;

import com.cyc.xiangwei.common.utils.Result;
import com.cyc.xiangwei.order.entity.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "xiangwei-commodity")
public interface ProductFeignClient {
    @GetMapping("/product/user/productOne/{id}")
    Result<?> getProductById(@PathVariable("id") Integer id);
    @PutMapping("/product/internal/deductStock")
    Result<?> deductStock(@RequestBody Product product);
}
