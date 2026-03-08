package com.cyc.xiangwei.order.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyc.xiangwei.order.dao.OrderMapper;
import com.cyc.xiangwei.order.entity.Order;
import com.cyc.xiangwei.order.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StatisticsTask {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate; // 假设你在网关或公共模块已经配好了 Redis

    /**
     * 核心亮点：离线跑批任务
     * 每天凌晨 2:00 执行，将白天消耗内存的 Stream 计算转移到服务器低谷期
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncAllMerchantStatsToRedis() {
        log.info("【离线跑批任务】开始执行：提取商家看板数据预热至 Redis...");
        try {
            // 1. 获取所有有订单的商家 ID（利用 MyBatis-Plus 去重）
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT merchant_id").isNotNull("merchant_id");
            List<Order> orders = orderMapper.selectList(wrapper);

            if (orders == null || orders.isEmpty()) return;
            List<Integer> merchantIds = orders.stream().map(Order::getMerchantId).collect(Collectors.toList());

            // 2. 遍历这些商家，利用你写好的 Service 提前算出折线图和饼图数据
            for (Integer merchantId : merchantIds) {
                Map<String, Object> map = new HashMap<>();
                map.put("daily", statisticsService.getDailySales(merchantId));
                map.put("pie", statisticsService.getProductPieData(merchantId));

                // 3. 将计算好的轻量级 JSON 存入 Redis，Key 为商家专属
                String redisKey = "mall:stat:merchant:" + merchantId;
                // 设置缓存存活 25 小时（保证撑到第二天凌晨的新跑批覆盖）
                redisTemplate.opsForValue().set(redisKey, map, 25, TimeUnit.HOURS);

                log.info("商家 {} 的大屏数据预热完成", merchantId);
            }
            log.info("【离线跑批任务】全部执行完毕！OOM 风险已彻底解除！");
        } catch (Exception e) {
            log.error("【离线跑批任务】执行失败！", e);
        }
    }
}
