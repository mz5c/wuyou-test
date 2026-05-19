package com.wuyou.onlytest.tx.dto;

import com.wuyou.onlytest.tx.entity.TxAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(name = "TxAccountVO", description = "账户信息")
public class TxAccountVO {
    @Schema(description = "账户ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "账户编号")
    private String accountNo;

    @Schema(description = "余额")
    private BigDecimal balance;

    @Schema(description = "状态 1-正常 2-冻结 3-销户")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public static TxAccountVO from(TxAccount account) {
        TxAccountVO vo = new TxAccountVO();
        vo.setId(account.getId());
        vo.setUserId(account.getUserId());
        vo.setAccountNo(account.getAccountNo());
        vo.setBalance(account.getBalance());
        vo.setStatus(account.getStatus());
        vo.setCreateTime(account.getCreateTime());
        return vo;
    }
}
