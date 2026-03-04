package com.cyc.xiangwei.order.feign;

import com.cyc.xiangwei.common.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "xiangwei-auth")
public interface UserFeignClient {
    @GetMapping("/user/internal/getUserOne/{id}")
    Result<?> getUserById(@PathVariable("id") Integer id);
}
