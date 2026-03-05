package com.cyc.xiangwei.order.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyc.xiangwei.common.utils.Result;
import com.cyc.xiangwei.order.dao.CartMapper;
import com.cyc.xiangwei.order.dao.OrderItemMapper;
import com.cyc.xiangwei.order.dao.OrderMapper;
import com.cyc.xiangwei.order.entity.*;
import com.cyc.xiangwei.order.entity.vo.OrderVo;
import com.cyc.xiangwei.order.feign.ProductFeignClient;
import com.cyc.xiangwei.order.feign.UserFeignClient;
import com.cyc.xiangwei.order.service.DeliveryService;
import com.cyc.xiangwei.order.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemMapper orderItemMapper;

    private final CartMapper cartMapper;

    private final DeliveryService deliveryService;
    private final ProductFeignClient productFeignClient;
    private final UserFeignClient userFeignClient;

    public OrderServiceImpl(OrderItemMapper orderItemMapper,  CartMapper cartMapper,  DeliveryService deliveryService,
                            ProductFeignClient productFeignClient, UserFeignClient userFeignClient) {
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
        this.deliveryService = deliveryService;
        this.productFeignClient = productFeignClient;
        this.userFeignClient = userFeignClient;
    }

    @Override
    public IPage<Order> getOrderByOrderId(String productName, Integer userId, Integer pageNum, Integer pageSize) {
        // 使用 MyBatis-Plus 自动分页
        Page<Order> page = new Page<>(pageNum, pageSize);
        List<Order> orders = baseMapper.findOrderWithItemsByProductName(page, userId, productName);
        page.setRecords(orders);
        return page;
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Integer orderId, Integer userId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在或无权限");
        }
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void addOrder(Order orderParam, List<OrderItem> orderItems, Integer userId) {

        // 0. 参数校验
        if (userId == null) {
            throw new RuntimeException("用户未登录或 userId 为空");
        }

        // 1. 按 merchantId 分组
        Map<Integer, List<OrderItem>> merchantMap = new HashMap<>();

        for (OrderItem item : orderItems) {

            // 1.1 查询商品
//            Product dbProduct = productService.getById(item.getProductId());
            Product dbProduct=null;
            Result<?> result = productFeignClient.getProductById(item.getProductId());
            if ("200".equals(result.getCode()) && result.getData() != null) {
                Object data = result.getData();
                ObjectMapper mapper = new ObjectMapper();
                dbProduct = mapper.convertValue(data, Product.class);

            }
            if (dbProduct == null) {
                throw new RuntimeException("商品不存在：" + item.getProductId());
            }

            // 1.2 校验库存
            if (dbProduct.getStock() == null || dbProduct.getStock() < item.getQuantity()) {
                throw new RuntimeException("商品【" + dbProduct.getName() + "】库存不足");
            }

            // 1.3 扣库存（简单版：读-改-写。并发严格方案见文末）
            dbProduct.setStock(dbProduct.getStock() - item.getQuantity());
//            boolean ok = productService.updateById(dbProduct);
            Result<?> result1 = productFeignClient.deductStock(dbProduct);

            if (result1 == null || !"200".equals(result1.getCode())) {
                throw new RuntimeException("扣减库存失败，商品：" + dbProduct.getId());
            }

            // 1.4 回填订单项真实数据（名称、单价、商家等）
            item.setProductName(dbProduct.getName());
            item.setPrice(dbProduct.getPrice());
            // item.setMerchantId(dbProduct.getMerchantId());

            Integer merchantId = dbProduct.getMerchantId();
            if (merchantId == null) {
                throw new RuntimeException("商品未绑定商家，商品：" + dbProduct.getId());
            }

            // 1.5 分组：一定要把 list 放回 map（你原来这里漏了 put）
            merchantMap.computeIfAbsent(merchantId, k -> new ArrayList<>()).add(item);
        }

        // 2. 每个商家生成一个订单 + 订单项
        for (Map.Entry<Integer, List<OrderItem>> entry : merchantMap.entrySet()) {

            Integer merchantId = entry.getKey();
            List<OrderItem> items = entry.getValue();

            // 2.1 计算该商家拆单的总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItem item : items) {
                BigDecimal lineAmount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                totalAmount = totalAmount.add(lineAmount);
            }

            // 2.2 创建订单
            Order order = new Order();
            order.setUserId(userId);
            order.setMerchantId(merchantId);
            order.setTotalAmount(totalAmount);

            // 状态：你原来写 1，保持一致。建议用枚举/常量
            order.setStatus(1);

            order.setCreateTime(LocalDateTime.now());

            // 如果你有 address、receiver、phone 等信息来自 orderParam，可在这里补齐
            // 例如：
            // if (orderParam != null) {
            //     order.setAddress(orderParam.getAddress());
            //     order.setReceiver(orderParam.getReceiver());
            //     order.setPhone(orderParam.getPhone());
            // }

            int insertRows = baseMapper.insert(order);
            if (insertRows != 1 || order.getId() == null) {
                throw new RuntimeException("订单创建失败（插入失败或未生成ID）");
            }

            // 2.3 插入订单项
            for (OrderItem item : items) {
                item.setOrderId(order.getId());
                // 可选：如果订单项表里要记录 userId/merchantId 等，也可以补齐
                // item.setUserId(userId);
                // item.setMerchantId(merchantId);

                int itemInsertRows = orderItemMapper.insert(item);
                if (itemInsertRows != 1) {
                    throw new RuntimeException("订单项插入失败，商品：" + item.getProductId());
                }
            }
        }

        // 3. 清空购物车（按用户）
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId));
    }


    @Override
    public IPage<OrderVo> getMerchantOrderList(Integer merchantId, Integer pageNum, Integer pageSize,Integer status,Integer orderId) {
        // 查询商家订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMerchantId, merchantId)
                .orderByDesc(Order::getCreateTime);
        if(status != 0){
            wrapper.eq(Order::getStatus, status);
        }
        if(orderId != null){
            wrapper.like(Order::getId, orderId);
        }

        Page<Order> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        // 转换成 OrderVo 列表
        List<OrderVo> orderVos = new ArrayList<>();
        for (Order record : page.getRecords()) {
            OrderVo orderVo = new OrderVo();
            // 拷贝 Order 的字段
            BeanUtils.copyProperties(record, orderVo);

            // 获取用户信息
//            User dbUser = userService.getById(record.getUserId());
            Result<?> result = userFeignClient.getUserById(record.getUserId());
            User dbUser = null;
            if ("200".equals(result.getCode()) && result.getData() != null) {
                Object data = result.getData();
                ObjectMapper mapper = new ObjectMapper();
                dbUser = mapper.convertValue(data, User.class);
            }

            if (dbUser != null) {
                orderVo.setUsername(dbUser.getUsername());
                orderVo.setAddress(dbUser.getAddress());
                orderVo.setPhone(dbUser.getPhone());
            }

            orderVos.add(orderVo);
        }

        // 构造分页返回
        Page<OrderVo> orderVoPage = new Page<>();
        BeanUtils.copyProperties(page, orderVoPage); // 拷贝分页信息
        orderVoPage.setRecords(orderVos);            // 设置新的记录列表

        return orderVoPage;
    }

    @Override
    public List<OrderItem> getMerchantOrderItems(Integer merchantId, Integer orderId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("订单不存在或无权查看");
        }

        return orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    //确认发货
    public void confirmReceiptService(Integer orderId, Integer userId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在或无权限");
        }
        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态不允许确认收货");
        }
        order.setStatus(3);
        // 3. 查询当前有效的物流（只允许已发货状态）
        OrderDelivery delivery = deliveryService.getOne(
                new LambdaQueryWrapper<OrderDelivery>()
                        .eq(OrderDelivery::getOrderId, orderId)
                        .eq(OrderDelivery::getStatus, 1) // 已发货
                        .last("LIMIT 1")
        );
        if (delivery == null) {
            throw new RuntimeException("未找到可确认的物流信息");
        }
        delivery.setStatus(2);
        deliveryService.updateById(delivery);
        order.setStatus(3);
        baseMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void after(Integer orderId, OrderDelivery orderDelivery) {
        Order order = baseMapper.selectById(orderId);
        if (order == null ) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 3) {
            throw new RuntimeException("订单状态不允许售后");
        }

        boolean existsReturn = deliveryService.count(
                new LambdaQueryWrapper<OrderDelivery>()
                        .eq(OrderDelivery::getOrderId, orderId)
                        .eq(OrderDelivery::getStatus, 4)
        ) > 0;
        if (existsReturn) {
            throw new RuntimeException("退货物流已创建，请勿重复提交");
        }

        // 取“最新一条已签收”的正向物流
        OrderDelivery delivery = deliveryService.getOne(
                new LambdaQueryWrapper<OrderDelivery>()
                        .eq(OrderDelivery::getOrderId, orderId)
                        .eq(OrderDelivery::getStatus, 2)
                        .orderByDesc(OrderDelivery::getShipTime)
                        .last("LIMIT 1")
        );
        if (delivery == null) {
            throw new RuntimeException("未找到可确认的物流信息");
        }

        OrderDelivery newDelivery = new OrderDelivery();
        newDelivery.setOrderId(orderId);
        newDelivery.setStatus(4); // 退货寄回/退货物流已创建（需你定义清楚）
        newDelivery.setShipTime(LocalDateTime.now());
        newDelivery.setExpressNo(orderDelivery.getExpressNo());
        newDelivery.setExpressCompany(orderDelivery.getExpressCompany());

        deliveryService.save(newDelivery);

        order.setStatus(4);
        baseMapper.updateById(order);
    }



}
