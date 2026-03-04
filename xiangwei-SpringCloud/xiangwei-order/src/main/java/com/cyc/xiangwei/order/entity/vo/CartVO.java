package com.cyc.xiangwei.order.entity.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CartVO {
    private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer quantity;
    private Date createTime;

    // 以下是从商品微服务拉取过来的数据
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
}