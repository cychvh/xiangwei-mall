package com.cyc.xiangwei.order.controller;

import com.cyc.xiangwei.common.utils.Result;
import com.cyc.xiangwei.common.utils.ResultCodeEnum;
import com.cyc.xiangwei.order.service.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    // 💡 辅助方法：安全地从网关透传的 Header 中提取 Integer 参数
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

    @GetMapping("/getAllStats")
    public Result<?> getAllStats(HttpServletRequest request) {
        // 微服务改造：放弃 attribute，从 Header 获取网关拦截器写入的用户信息
        Integer merchantId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (merchantId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 1) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        // 返回包含折线图和饼图双列表的复合结果
        Map<String, Object> map = new HashMap<>();
        map.put("daily", statisticsService.getDailySales(merchantId));
        map.put("pie", statisticsService.getProductPieData(merchantId));

        return Result.success(map);
    }
}
