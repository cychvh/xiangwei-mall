package com.cyc.xiangwei.order.service;

import com.cyc.xiangwei.order.entity.vo.DailySalesVO;
import com.cyc.xiangwei.order.entity.vo.ProductPieVO;

import java.util.List;

public interface StatisticsService {
    // 获取商家每日销售额趋势
    List<DailySalesVO> getDailySales(Integer merchantId);

    // 获取商家商品销量饼图数据
    List<ProductPieVO> getProductPieData(Integer merchantId);
}