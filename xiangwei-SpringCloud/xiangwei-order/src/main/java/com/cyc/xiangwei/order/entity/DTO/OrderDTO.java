package com.cyc.xiangwei.order.entity.DTO;

import com.cyc.xiangwei.order.entity.Order;
import com.cyc.xiangwei.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderDTO {
    private Order order;
    private List<OrderItem> orderItems;
}
