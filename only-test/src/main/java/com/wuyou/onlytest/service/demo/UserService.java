package com.wuyou.onlytest.service.demo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuyou.common.page.PageResult;
import com.wuyou.onlytest.entity.demo.User;
import com.wuyou.onlytest.mapper.demo.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    public PageResult<User> pageUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                   .or().like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getId);
        Page<User> result = baseMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }
}
