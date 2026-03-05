package com.cyc.xiangwei.order.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPieVO {
    // 商品名称
    private String name;
    // 销量总数
    private Integer value;
}
