package com.cyc.xiangwei.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyc.xiangwei.order.entity.OrderDelivery;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeliveryMapper extends BaseMapper<OrderDelivery> {
}
