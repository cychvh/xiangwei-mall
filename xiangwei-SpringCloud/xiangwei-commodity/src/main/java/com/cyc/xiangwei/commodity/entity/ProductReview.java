package com.cyc.xiangwei.commodity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    private Integer productId;
    private Integer userId;
    private Integer orderId;

    /**
     * 评分：1到5星
     */
    private Integer rating;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论附带的图片(OSS链接，多张图用逗号隔开)
     */
    private String images;

    private Date createTime;


}