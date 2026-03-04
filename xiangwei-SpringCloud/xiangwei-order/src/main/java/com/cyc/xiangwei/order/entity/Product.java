package com.cyc.xiangwei.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    // 物品的种类名
    private String categoryname;
    private BigDecimal price;
    // 库存
    private Integer stock;
    // 产地的地址
    private String originplace;
    // 图片的地址(OSS)
    private String imageurl;
    // 状态：0-下架, 1-上架
    private Integer status;

    private Integer merchantId;

    private Date createTime;
    private Date updateTime;
}
