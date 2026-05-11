package com.wuyou.onlytest.seata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuyou.onlytest.seata.entity.Account;
import org.apache.ibatis.annotations.Select;

public interface AccountMapper extends BaseMapper<Account> {
    @Select("SELECT * FROM seata_account WHERE user_id = #{userId}")
    Account selectByUserId(Long userId);
}
