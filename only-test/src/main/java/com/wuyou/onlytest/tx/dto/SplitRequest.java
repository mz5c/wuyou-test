package com.wuyou.onlytest.tx.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Data
@Schema(name = "SplitRequest", description = "金额分摊请求")
public class SplitRequest {
    @NotNull(message = "总金额不能为空")
    @DecimalMin(value = "0.01", message = "总金额至少为 0.01")
    @Schema(description = "待分摊总金额", example = "10.00")
    private BigDecimal totalAmount;

    @NotEmpty(message = "比例列表不能为空")
    @Schema(description = "分摊比例列表", example = "[1, 1, 1]")
    private List<BigDecimal> ratios;

    @Schema(description = "小数位数，默认2位", example = "2")
    private Integer scale;

    @Schema(description = "舍入模式，默认 HALF_EVEN", example = "HALF_EVEN")
    private RoundingMode roundingMode;

    public int getScale() {
        return scale != null ? scale : 2;
    }

    public RoundingMode getRoundingMode() {
        return roundingMode != null ? roundingMode : RoundingMode.HALF_EVEN;
    }
}
