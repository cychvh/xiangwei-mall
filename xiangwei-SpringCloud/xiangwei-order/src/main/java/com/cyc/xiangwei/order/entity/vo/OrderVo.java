package com.cyc.xiangwei.order.entity.vo;

import com.cyc.xiangwei.order.entity.Order;
import lombok.Data;

@Data
public class OrderVo extends Order {
    private String username;
    private String address;
    private String phone;
}