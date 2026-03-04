package com.cyc.xiangwei.order.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyc.xiangwei.order.dao.DeliveryMapper;
import com.cyc.xiangwei.order.dao.OrderMapper;
import com.cyc.xiangwei.order.entity.Order;
import com.cyc.xiangwei.order.entity.OrderDelivery;
import com.cyc.xiangwei.order.service.DeliveryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DeliveryServiceImpl extends ServiceImpl<DeliveryMapper, OrderDelivery> implements DeliveryService {

    private final OrderMapper orderMapper;

    public DeliveryServiceImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public void shipOrder(Integer orderId, Integer merchantId, String expressCompany, String expressNo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权操作该订单");
        }

        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态不允许发货");
        }
        OrderDelivery exist = baseMapper.selectOne(
                new LambdaQueryWrapper<OrderDelivery>()
                        .eq(OrderDelivery::getOrderId, orderId)
                        .eq(OrderDelivery::getStatus, 1)
        );
        if (exist != null) {
            throw new RuntimeException("订单已发货");
        }
        OrderDelivery delivery = new OrderDelivery();
        delivery.setOrderId(orderId);
        delivery.setMerchantId(merchantId);
        delivery.setExpressCompany(expressCompany);
        delivery.setExpressNo(expressNo);
        delivery.setShipTime(LocalDateTime.now());
        delivery.setStatus(1);

        this.save(delivery);
        order.setStatus(2);
        orderMapper.updateById(order);
    }
    @Override
    @Transactional
    public void addOrUpdateDelivery(OrderDelivery newDelivery) {

        Order order = orderMapper.selectById(newDelivery.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 只能在「待收货」阶段修改物流
        if (order.getStatus() != 2) {
            throw new RuntimeException("当前订单状态不允许修改物流");
        }

        OrderDelivery oldDelivery = baseMapper.selectOne(
                new LambdaQueryWrapper<OrderDelivery>()
                        .eq(OrderDelivery::getOrderId, newDelivery.getOrderId())
                        .eq(OrderDelivery::getStatus, 1)
                        .orderByDesc(OrderDelivery::getShipTime)
                        .last("LIMIT 1")
        );

        if (oldDelivery == null) {
            throw new RuntimeException("不存在可修改的物流记录");
        }

        // 作废旧物流
        oldDelivery.setStatus(3);
        updateById(oldDelivery);

        // 新增新物流
        newDelivery.setId(null);
        newDelivery.setStatus(1);
        newDelivery.setShipTime(LocalDateTime.now());
        save(newDelivery);
    }

    @Override
    public OrderDelivery getDelivery(Integer orderId) {
        OrderDelivery orderDelivery = baseMapper.selectOne(new LambdaQueryWrapper<OrderDelivery>()
                .eq(OrderDelivery::getOrderId, orderId)
                .eq(OrderDelivery::getStatus, 1)
                .orderByDesc(OrderDelivery::getShipTime)
                .last("LIMIT 1"));
        if (orderDelivery == null) {
            throw new RuntimeException("未找到的物流信息");
        }
        return orderDelivery;
    }


}
