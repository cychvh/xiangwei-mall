package com.cyc.xiangwei.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cyc.xiangwei.system.entity.Notice;


public interface NoticeService extends IService<Notice> {
    public IPage<Notice> getNoticeList(Integer pageNum,Integer pageSize,Integer type);
}
