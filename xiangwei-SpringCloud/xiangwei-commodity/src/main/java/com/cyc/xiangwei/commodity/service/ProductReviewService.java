package com.cyc.xiangwei.commodity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyc.xiangwei.commodity.entity.ProductReview;

public interface ProductReviewService extends IService<ProductReview> {
    void addReview(ProductReview review, Integer userId);
}
