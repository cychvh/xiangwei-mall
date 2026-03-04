package com.cyc.xiangwei.commodity.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyc.xiangwei.commodity.dao.ProductReviewMapper;
import com.cyc.xiangwei.commodity.entity.ProductReview;
import com.cyc.xiangwei.commodity.service.ProductReviewService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ProductReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview> implements ProductReviewService {

    @Override
    public void addReview(ProductReview review, Integer userId) {
        // 1. 强制设置当前登录人为评论人，防止越权伪造
        review.setUserId(userId);
        review.setCreateTime(new Date());

        // 💡 进阶预留：这里未来需要通过 OpenFeign 调用 xiangwei-order 服务
        // 校验该 orderId 是否真的属于该 userId，且订单状态是否为 "3-已完成"
        // 伪代码:
        // Result<OrderDTO> orderResult = orderFeignClient.getOrderById(review.getOrderId());
        // if(orderResult == null || !orderResult.getUserId().equals(userId) || orderResult.getStatus() != 3) {
        //     throw new RuntimeException("无权评价该订单");
        // }

        // 2. 插入评论数据
        baseMapper.insert(review);
    }
}
