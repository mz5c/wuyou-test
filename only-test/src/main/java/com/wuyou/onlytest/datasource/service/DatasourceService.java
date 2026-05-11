package com.wuyou.onlytest.datasource.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.wuyou.onlytest.entity.demo.User;
import com.wuyou.onlytest.mapper.demo.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatasourceService {

    private final UserMapper userMapper;

    @DS("slave")
    public List<User> listUsersFromSlave() {
        log.info("querying users from slave");
        return userMapper.selectList(null);
    }

    @DS("master")
    @Transactional(rollbackFor = Exception.class)
    public void createUserOnMaster(String username, String nickname) {
        log.info("creating user on master: {}", username);
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        userMapper.insert(user);
    }

    @DS("master")
    public User getUserFromMaster(Long id) {
        log.info("querying user {} from master", id);
        return userMapper.selectById(id);
    }
}
