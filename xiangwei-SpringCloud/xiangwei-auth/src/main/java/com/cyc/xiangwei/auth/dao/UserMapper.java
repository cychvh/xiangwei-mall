package com.cyc.xiangwei.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.cyc.xiangwei.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
