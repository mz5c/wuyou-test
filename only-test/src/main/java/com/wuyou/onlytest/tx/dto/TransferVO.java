package com.wuyou.onlytest.tx.dto;

import com.wuyou.onlytest.tx.entity.TransferRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(name = "TransferVO", description = "转账记录")
public class TransferVO {
    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "业务编号")
    private String bizNo;

    @Schema(description = "转出账户")
    private String fromAccountNo;

    @Schema(description = "转入账户")
    private String toAccountNo;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "状态 0-处理中 1-成功 2-失败")
    private Integer status;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public static TransferVO from(TransferRecord record) {
        TransferVO vo = new TransferVO();
        vo.setId(record.getId());
        vo.setBizNo(record.getBizNo());
        vo.setFromAccountNo(record.getFromAccountNo());
        vo.setToAccountNo(record.getToAccountNo());
        vo.setAmount(record.getAmount());
        vo.setStatus(record.getStatus());
        vo.setFailReason(record.getFailReason());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }
}
