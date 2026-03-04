package com.cyc.xiangwei.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@TableName("order_delivery")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderDelivery {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;
    private Integer merchantId;
    private String expressCompany;
    private String expressNo;
    private LocalDateTime shipTime;
    private Integer status;
}
