package com.wuyou.onlytest.mapper.demo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuyou.onlytest.entity.demo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询所有用户记录（包含逻辑删除的），绕过 @TableLogic 自动过滤
     */
    @Select("SELECT * FROM demo_user")
    List<User> selectAllIncludeDeleted();
}
