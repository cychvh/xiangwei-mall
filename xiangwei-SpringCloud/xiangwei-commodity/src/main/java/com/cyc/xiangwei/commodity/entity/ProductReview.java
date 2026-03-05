package com.cyc.xiangwei.commodity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@TableName("product_review")
public class ProductReview {

    @TableId(type = IdType.AUTO)
    private Integer id;
    @NotNull(message = "商品ID不能为空")
    private Integer productId;
    private Integer userId;
    @NotNull(message = "订单ID不能为空")
    private Integer orderId;

    /**
     * 评分：1到5星
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1星")
    @Max(value = 5, message = "评分最高为5星")
    private Integer rating;

    /**
     * 评论内容
     */
    @NotBlank(message = "评价内容不能为空")
    private String content;

    /**
     * 评论附带的图片(OSS链接，多张图用逗号隔开)
     */
    private String images;

    private Date createTime;


}