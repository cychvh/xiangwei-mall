package com.cyc.xiangwei.commodity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyc.xiangwei.commodity.entity.ProductReview;
import com.cyc.xiangwei.commodity.service.ProductReviewService;
import com.cyc.xiangwei.common.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product/review")
public class ProductReviewController {

    private final ProductReviewService reviewService;

    public ProductReviewController(ProductReviewService reviewService) {
        this.reviewService = reviewService;
    }

    private Integer getIntegerHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 用户发表商品评论
     */
    @PostMapping("/add")
    public Result<?> addReview(@RequestBody ProductReview review, HttpServletRequest request) {
        Integer userId = getIntegerHeader(request, "userId");
        if (userId == null) {
            return Result.error("401", "未登录，无法评价");
        }

        try {
            reviewService.addReview(review, userId);
            return Result.success("评价成功");
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    /**
     * 分页获取某件商品的所有评论（公开接口，任何人可看）
     */
    @GetMapping("/list/{productId}")
    public Result<?> getProductReviews(@PathVariable Integer productId,
                                       @RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize) {

        LambdaQueryWrapper<ProductReview> query = new LambdaQueryWrapper<>();
        query.eq(ProductReview::getProductId, productId)
                .orderByDesc(ProductReview::getCreateTime); // 按时间倒序展示最新评论

        Page<ProductReview> page = reviewService.page(new Page<>(pageNum, pageSize), query);
        return Result.success(page);
    }
}
