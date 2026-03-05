package com.cyc.xiangwei.commodity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "商品名称不能为空")
    private String name;
    // 物品的种类名
    @NotBlank(message = "商品分类不能为空")
    private String categoryname;
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0, message = "商品价格不能小于0")
    private BigDecimal price;
    // 库存
    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存不能小于0")
    private Integer stock;
    // 产地的地址
    private String originplace;
    // 图片的地址(OSS)
    @NotBlank(message = "商品图片不能为空")
    private String imageurl;
    // 状态：0-下架, 1-上架
    private Integer status;

    private Integer merchantId;

    private Date createTime;
    private Date updateTime;
}
