package com.cyc.xiangwei.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyc.xiangwei.order.entity.OrderDelivery;

public interface DeliveryService extends IService<OrderDelivery> {
    public void shipOrder(Integer orderId, Integer merchantId, String expressCompany, String expressNo);
    public void addOrUpdateDelivery(OrderDelivery newDelivery);
    public OrderDelivery getDelivery(Integer orderId);
}
