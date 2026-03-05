package com.cyc.xiangwei.order.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailySalesVO {
    // 日期，格式 "yyyy-MM-dd"
    private String date;
    // 当日订单数
    private Integer orderCount;
    // 当日总销售额
    private BigDecimal totalAmount;
}
