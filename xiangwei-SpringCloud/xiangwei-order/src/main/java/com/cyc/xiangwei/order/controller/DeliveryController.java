package com.cyc.xiangwei.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyc.xiangwei.common.utils.Result;
import com.cyc.xiangwei.order.entity.OrderDelivery;
import com.cyc.xiangwei.order.service.DeliveryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // 💡 辅助方法：安全地从 Header 提取 Integer 类型的参数
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

    // 1. 添加物流 (发货)
    @PostMapping("/ship")
    public Result<?> ship(@Validated @RequestBody OrderDelivery orderDelivery,
                          HttpServletRequest request) {

        Integer merchantId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        // 权限校验：必须登录，且必须是商家 (type == 1)
        if (merchantId == null || type == null || type != 1) {
            return Result.error("405", "权限不足，非商家无权发货");
        }

        deliveryService.shipOrder(orderDelivery.getOrderId(), merchantId, orderDelivery.getExpressCompany(), orderDelivery.getExpressNo());
        return Result.success();
    }

    // 2. 展示商家的全部物流信息
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,   // 加上默认值防止报错
                          @RequestParam(defaultValue = "10") Integer pageSize, // 加上默认值防止报错
                          @RequestParam(required = false) Integer orderId,
                          HttpServletRequest request) {

        Integer merchantId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (merchantId == null || type == null || type != 1) {
            return Result.error("405", "权限不足，非商家无权查看");
        }

        LambdaQueryWrapper<OrderDelivery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDelivery::getMerchantId, merchantId);

        if (orderId != null) {
            wrapper.eq(OrderDelivery::getOrderId, orderId);
        }

        wrapper.orderByDesc(OrderDelivery::getShipTime);

        Page<OrderDelivery> page = deliveryService.page(new Page<>(pageNum, pageSize), wrapper);

        return Result.success(page);
    }

    // 3. 修改物流信息
    @PutMapping("/correct")
    public Result<?> correctDelivery(@RequestBody OrderDelivery orderDelivery,
                                     HttpServletRequest request) {

        Integer merchantId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (merchantId == null || type == null || type != 1) {
            return Result.error("405", "权限不足，非商家无权修改物流");
        }

        // 强制使用当前商户 ID，防止商家篡改别人的物流
        orderDelivery.setMerchantId(merchantId);

        // 核心业务全部交给 Service
        deliveryService.addOrUpdateDelivery(orderDelivery);

        return Result.success();
    }

    // 4. 获取单条物流轨迹信息
    @GetMapping("/getOne")
    public Result<?> getOne(@RequestParam Integer orderId, HttpServletRequest servletRequest) {

        Integer type = getIntegerHeader(servletRequest, "type");
        if (type == null) {
            return Result.error("401", "未登录");
        }

        if (type != 2 && type != 1 && type != 0) {
            return Result.error("405", "权限不足");
        }

        OrderDelivery delivery = null;
        try {
            delivery = deliveryService.getDelivery(orderId);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }

        return Result.success(delivery);
    }
}