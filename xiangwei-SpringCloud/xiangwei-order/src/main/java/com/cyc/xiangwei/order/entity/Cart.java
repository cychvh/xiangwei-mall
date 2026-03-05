package com.cyc.xiangwei.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 购物车实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("cart") // 映射数据库的 cart 表
public class Cart {

    // 购物车记录ID，主键自增
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 用户ID
    private Integer userId;

    // 商品ID
    @NotNull(message = "商品ID不能为空")
    private Integer productId;

    // 加购数量
    @NotNull(message = "加购数量不能为空")
    @Min(value = 1, message = "加购数量必须大于0")
    private Integer quantity;

    // 加购时间
    private Date createTime;
}