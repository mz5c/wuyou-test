package com.wuyou.onlytest.tx.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@Schema(name = "SplitResult", description = "分摊结果")
public class SplitResult {
    @Schema(description = "分摊后的金额列表")
    private List<BigDecimal> parts;

    @Schema(description = "分摊总和（应等于原总额）")
    private BigDecimal sum;

    @Schema(description = "原始总额")
    private BigDecimal totalAmount;
}
