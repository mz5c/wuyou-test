package com.wuyou.onlytest.mapper.demo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuyou.onlytest.entity.demo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
