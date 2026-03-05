package com.cyc.xiangwei.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "订单ID不能为空")
    private Integer orderId;
    private Integer merchantId;
    @NotBlank(message = "快递公司不能为空")
    private String expressCompany;
    @NotBlank(message = "快递单号不能为空")
    private String expressNo;
    private LocalDateTime shipTime;
    private Integer status;
}
