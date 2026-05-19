package com.wuyou.onlytest.tx.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "TransferRequest", description = "转账请求")
public class TransferRequest {
    @NotBlank(message = "转出账户不能为空")
    @Schema(description = "转出账户", example = "ACC10001")
    private String fromAccountNo;

    @NotBlank(message = "转入账户不能为空")
    @Schema(description = "转入账户", example = "ACC10002")
    private String toAccountNo;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额至少为 0.01")
    @Schema(description = "转账金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "业务编号(幂等)，为空自动生成", example = "BIZ2026001")
    private String bizNo;
}
