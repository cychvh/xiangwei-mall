package com.cyc.xiangwei.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyc.xiangwei.common.utils.Result;
import com.cyc.xiangwei.common.utils.ResultCodeEnum;
import com.cyc.xiangwei.order.entity.DTO.OrderDTO;
import com.cyc.xiangwei.order.entity.Order;
import com.cyc.xiangwei.order.entity.OrderDelivery;
import com.cyc.xiangwei.order.entity.OrderItem;
import com.cyc.xiangwei.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 辅助方法：安全地从 Header 提取 Integer 类型的参数
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

    // 买家查询自己的订单
    @GetMapping("/userList")
    public Result<?> userList(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "5") Integer pageSize,
                              @RequestParam(defaultValue = "") String productName,
                              HttpServletRequest request) {
        // 微服务改造：从 Header 获取数据
        Integer userId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 2) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        IPage<Order> orderByOrderId = orderService.getOrderByOrderId(productName, userId, pageNum, pageSize);
        return Result.success(orderByOrderId);
    }

    // 管理员查询全部订单情况
    @GetMapping("/adminList")
    public Result<?> adminList(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "5") Integer pageSize,
                               @RequestParam(required = false) Integer orderId,
                               HttpServletRequest request) {

        Integer type = getIntegerHeader(request, "type");
        if (type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 0) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        LambdaQueryWrapper<Order> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (orderId != null) {
            orderLambdaQueryWrapper.eq(Order::getId, orderId);
        }
        Page<Order> page = orderService.page(new Page<>(pageNum, pageSize), orderLambdaQueryWrapper);
        return Result.success(page);
    }

    // 查询订单明细项
    @GetMapping("/getOrderItem")
    public Result<?> getOrderItem(@RequestParam Integer orderId, HttpServletRequest request) {
        Integer userId = getIntegerHeader(request, "userId");

        if (userId == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }


        List<OrderItem> orderItemsByOrderId = orderService.getOrderItemsByOrderId(orderId, userId);
        return Result.success(orderItemsByOrderId);


    }

    // 买家下单
    @PostMapping("/addOrder")
    public Result<?> addOrder(@Validated @RequestBody OrderDTO orderDTO, HttpServletRequest request) {
        Integer userId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 2) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }


        Order order = orderDTO.getOrder();
        List<OrderItem> orderItems = orderDTO.getOrderItems();
        orderService.addOrder(order, orderItems, userId);
        return Result.success();
    }

    // 商家查询订单列表
    @GetMapping("/MerchantList")
    public Result<?> merchantList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(defaultValue = "0") Integer status,
                                  @RequestParam(required = false) Integer orderId,
                                  HttpServletRequest request) {

        Integer merchantId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (merchantId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 1) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        return Result.success(
                orderService.getMerchantOrderList(merchantId, pageNum, pageSize, status, orderId)
        );
    }

    // 商家查询订单明细项
    @GetMapping("/merchantOrderItems")
    public Result<?> getMerchantOrderItems(@RequestParam Integer orderId, HttpServletRequest request) {
        Integer merchantId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (merchantId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 1) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        List<OrderItem> items = orderService.getMerchantOrderItems(merchantId, orderId);
        return Result.success(items);

    }

    // 买家确认收货
    @PutMapping("/confirmReceipt")
    public Result<?> confirmReceipt(@RequestParam Integer orderId, HttpServletRequest request) {

        Integer userId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 2) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        if (orderId == null) {
            return Result.error(ResultCodeEnum.PARAM_ERROR, "订单ID不能为空");
        }

        orderService.confirmReceiptService(orderId, userId);
        return Result.success("确认收货成功");
    }

    // 售后申请
    @PutMapping("/after")
    public Result<?> after(@RequestParam Integer orderId, @Validated @RequestBody OrderDelivery orderDelivery, HttpServletRequest request) {
        Integer userId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 2) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }
        if (orderId == null) {
            return Result.error(ResultCodeEnum.PARAM_ERROR, "订单ID不能为空");
        }

        // 您的 after 逻辑目前在 Controller 里是空的，后续可以调用 orderService.after()
        orderService.after(orderId, orderDelivery);
        return Result.success();
    }

}