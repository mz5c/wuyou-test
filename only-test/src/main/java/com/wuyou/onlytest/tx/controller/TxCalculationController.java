package com.wuyou.onlytest.tx.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.tx.dto.SplitRequest;
import com.wuyou.onlytest.tx.dto.SplitResult;
import com.wuyou.onlytest.tx.service.TxCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "交易沙箱-金额计算")
@RestController
@RequestMapping("/api/v1/tx")
@RequiredArgsConstructor
public class TxCalculationController {

    private final TxCalculationService calculationService;

    @Operation(summary = "金额分摊（按比例，消除尾差）")
    @PostMapping("/calc/split")
    public Result<SplitResult> split(@Valid @RequestBody SplitRequest req) {
        return Result.success(calculationService.split(req));
    }
}
