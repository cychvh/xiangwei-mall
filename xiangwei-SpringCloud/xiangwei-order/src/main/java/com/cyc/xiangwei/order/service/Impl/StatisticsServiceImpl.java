package com.cyc.xiangwei.order.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyc.xiangwei.order.dao.OrderItemMapper;
import com.cyc.xiangwei.order.dao.OrderMapper;
import com.cyc.xiangwei.order.entity.Order;
import com.cyc.xiangwei.order.entity.OrderItem;
import com.cyc.xiangwei.order.entity.vo.DailySalesVO;
import com.cyc.xiangwei.order.entity.vo.ProductPieVO;
import com.cyc.xiangwei.order.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public StatisticsServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public List<DailySalesVO> getDailySales(Integer merchantId) {
        // 1. 从数据库仅查出该商家的有效订单 (状态 1已支付, 2已发货, 3已完成)
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMerchantId, merchantId)
                .in(Order::getStatus, Arrays.asList(1, 2, 3));
        List<Order> orders = orderMapper.selectList(wrapper);

        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 流式计算：按日期分组 -> 聚合订单数和金额 -> 排序 -> 取前15天
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return orders.stream()
                .collect(Collectors.groupingBy(order -> order.getCreateTime().format(formatter)))
                .entrySet().stream()
                .map(entry -> {
                    String date = entry.getKey();
                    List<Order> dailyOrders = entry.getValue();

                    DailySalesVO vo = new DailySalesVO();
                    vo.setDate(date);
                    vo.setOrderCount(dailyOrders.size());

                    // 累加当天的 totalAmount
                    BigDecimal totalAmount = dailyOrders.stream()
                            .map(Order::getTotalAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    vo.setTotalAmount(totalAmount);

                    return vo;
                })
                // 按日期倒序排列 (最新的日期在前)
                .sorted((v1, v2) -> v2.getDate().compareTo(v1.getDate()))
                .limit(15)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductPieVO> getProductPieData(Integer merchantId) {
        // 1. 查出有效订单的 ID 集合
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getMerchantId, merchantId)
                .in(Order::getStatus, Arrays.asList(1, 2, 3))
                .select(Order::getId); // 优化：只查 ID 字段节约内存
        List<Order> orders = orderMapper.selectList(orderWrapper);

        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());

        // 2. 用订单 IDs 查出所有的订单项 (用 in 替代了原先的 JOIN)
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getOrderId, orderIds);
        List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);

        // 3. 流式计算：按商品名称分组 -> 累加 quantity -> 排序 -> 取前10名
        return orderItems.stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getProductName,
                        Collectors.summingInt(OrderItem::getQuantity)
                ))
                .entrySet().stream()
                .map(entry -> new ProductPieVO(entry.getKey(), entry.getValue()))
                // 按销量倒序排列
                .sorted((v1, v2) -> v2.getValue().compareTo(v1.getValue()))
                .limit(10)
                .collect(Collectors.toList());
    }
}
