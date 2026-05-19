package com.wuyou.onlytest.tx.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tx_account")
public class TxAccount {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String accountNo;
    private BigDecimal balance;
    private BigDecimal frozen;
    private Integer status;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
