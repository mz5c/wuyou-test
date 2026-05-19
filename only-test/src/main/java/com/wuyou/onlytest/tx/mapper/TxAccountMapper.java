package com.wuyou.onlytest.tx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuyou.onlytest.tx.entity.TxAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TxAccountMapper extends BaseMapper<TxAccount> {

    @Select("SELECT * FROM tx_account WHERE account_no = #{accountNo} FOR UPDATE")
    TxAccount selectByNoForUpdate(@Param("accountNo") String accountNo);

    @Select("SELECT * FROM tx_account WHERE account_no = #{accountNo}")
    TxAccount selectByAccountNo(@Param("accountNo") String accountNo);

    @Select("SELECT * FROM tx_account WHERE user_id = #{userId}")
    TxAccount selectByUserId(@Param("userId") Long userId);
}
