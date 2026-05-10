package com.wuyou.onlytest.mapper.demo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuyou.onlytest.entity.demo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询所有用户记录（包含逻辑删除的），绕过 @TableLogic 自动过滤
     */
    @Select("SELECT * FROM demo_user")
    List<User> selectAllIncludeDeleted();

    /**
     * 根据 ID 查询用户（包含逻辑删除的），绕过 @TableLogic 自动过滤
     */
    @Select("SELECT * FROM demo_user WHERE id = #{id}")
    User selectByIdIncludeDeleted(Long id);

    /**
     * 恢复已逻辑删除的用户，绕过 @TableLogic 和 @Version 拦截
     */
    @Update("UPDATE demo_user SET deleted = 0 WHERE id = #{id}")
    int restoreById(Long id);
}
