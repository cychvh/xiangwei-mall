package com.cyc.xiangwei.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyc.xiangwei.order.entity.OrderDelivery;

public interface DeliveryService extends IService<OrderDelivery> {
    void shipOrder(Integer orderId, Integer merchantId, String expressCompany, String expressNo);

    void addOrUpdateDelivery(OrderDelivery newDelivery);

    OrderDelivery getDelivery(Integer orderId);
}
