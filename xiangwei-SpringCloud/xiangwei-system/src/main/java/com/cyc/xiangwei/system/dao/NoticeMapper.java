package com.cyc.xiangwei.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyc.xiangwei.system.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
    @Select("SELECT `id`,`date`,content from notice where type = #{type} or type = 'All' ORDER BY `date` DESC  ")
    IPage<Notice> selectByType(IPage<Notice>page, @Param("type") String type);
}

