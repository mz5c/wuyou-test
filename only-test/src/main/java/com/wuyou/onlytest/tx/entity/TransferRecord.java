package com.wuyou.onlytest.tx.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tx_transfer_record")
public class TransferRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String bizNo;
    private String fromAccountNo;
    private String toAccountNo;
    private BigDecimal amount;
    private Integer status;
    private String failReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
