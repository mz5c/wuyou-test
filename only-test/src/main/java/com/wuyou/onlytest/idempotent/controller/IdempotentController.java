package com.wuyou.onlytest.idempotent.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.idempotent.service.IdempotentDbService;
import com.wuyou.onlytest.idempotent.service.IdempotentRedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "幂等测试")
@RestController
@RequestMapping("/api/v1/idempotent")
@RequiredArgsConstructor
public class IdempotentController {

    private final IdempotentRedisService idempotentRedisService;
    private final IdempotentDbService idempotentDbService;

    @Operation(summary = "Redis 幂等方式支付")
    @PostMapping("/pay")
    public Result<String> pay(@RequestParam Long orderId, @RequestParam Long userId) {
        return Result.success(idempotentRedisService.payOrder(orderId, userId));
    }

    @Operation(summary = "DB 唯一键幂等方式支付")
    @PostMapping("/pay-db")
    public Result<String> payDb(@RequestParam String bizType, @RequestParam String bizId) {
        return Result.success(idempotentDbService.payOrder(bizType, bizId));
    }
}
