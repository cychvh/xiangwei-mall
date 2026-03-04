package com.cyc.xiangwei.system.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyc.xiangwei.system.dao.NoticeMapper;
import com.cyc.xiangwei.system.entity.Notice;
import com.cyc.xiangwei.system.service.NoticeService;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    public IPage<Notice> getNoticeList(Integer pageNum, Integer pageSize, Integer type) {
        // 直接使用 MyBatis Plus 的 Wrapper，查询已发布的公告，按时间倒序显示
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Notice::getCreateTime);

        return baseMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
    }
}