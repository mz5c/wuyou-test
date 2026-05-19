package com.wuyou.onlytest.tx.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.tx.dto.TxAccountVO;
import com.wuyou.onlytest.tx.service.TxAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Tag(name = "交易沙箱-账户")
@RestController
@RequestMapping("/api/v1/tx/accounts")
@RequiredArgsConstructor
public class TxAccountController {

    private final TxAccountService accountService;

    @Operation(summary = "开户")
    @PostMapping
    public Result<TxAccountVO> openAccount(@RequestParam Long userId) {
        return Result.success(accountService.openAccount(userId));
    }

    @Operation(summary = "查询账户")
    @GetMapping("/{id}")
    public Result<TxAccountVO> getById(@PathVariable Long id) {
        return Result.success(accountService.getById(id));
    }

    @Operation(summary = "充值")
    @PostMapping("/{id}/recharge")
    public Result<TxAccountVO> recharge(@PathVariable Long id,
                                        @RequestParam @Positive BigDecimal amount) {
        return Result.success(accountService.recharge(id, amount));
    }
}
