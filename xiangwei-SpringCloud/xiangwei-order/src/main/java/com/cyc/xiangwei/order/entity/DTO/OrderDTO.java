package com.cyc.xiangwei.order.entity.DTO;

import com.cyc.xiangwei.order.entity.Order;
import com.cyc.xiangwei.order.entity.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderItem> orderItems;
}
