package com.wuyou.onlytest.tx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wuyou.onlytest.tx.entity.TransferRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TransferRecordMapper extends BaseMapper<TransferRecord> {

    @Select("SELECT * FROM tx_transfer_record WHERE biz_no = #{bizNo}")
    TransferRecord selectByBizNo(@Param("bizNo") String bizNo);

    IPage<TransferRecord> selectPageByAccountNo(IPage<TransferRecord> page, @Param("accountNo") String accountNo);
}
