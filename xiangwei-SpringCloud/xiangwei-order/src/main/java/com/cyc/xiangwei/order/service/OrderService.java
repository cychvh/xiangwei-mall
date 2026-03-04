package com.cyc.xiangwei.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cyc.xiangwei.order.entity.Order;
import com.cyc.xiangwei.order.entity.OrderDelivery;
import com.cyc.xiangwei.order.entity.OrderItem;
import com.cyc.xiangwei.order.entity.vo.OrderVo;

import java.util.List;

public interface OrderService extends IService<Order> {
    IPage<Order> getOrderByOrderId(String productName, Integer userId, Integer PageNum, Integer PageSize);
    List<OrderItem> getOrderItemsByOrderId(Integer orderId, Integer userId);
    void addOrder(Order order, List<OrderItem> orderItems, Integer userId);
    public IPage<OrderVo> getMerchantOrderList(Integer merchantId, Integer pageNum, Integer pageSize,Integer status,Integer orderId);
    public List<OrderItem> getMerchantOrderItems(Integer merchantId, Integer orderId);
    public void confirmReceiptService(Integer orderId,Integer userId);
    void after(Integer orderId,  OrderDelivery orderDelivery);
}
