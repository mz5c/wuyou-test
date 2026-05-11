package com.wuyou.onlytest.ratelimit.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.ratelimit.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "限流测试")
@RestController
@RequestMapping("/api/v1/ratelimit")
@RequiredArgsConstructor
public class RateLimitController {

    private final RateLimitService rateLimitService;

    @Operation(summary = "本地限流（5 QPS）")
    @GetMapping("/local")
    public Result<String> local() {
        return Result.success(rateLimitService.localLimited());
    }

    @Operation(summary = "分布式限流（3次/秒）")
    @GetMapping("/distributed")
    public Result<String> distributed() {
        return Result.success(rateLimitService.distributedLimited());
    }

    @Operation(summary = "压力测试（快速连调观察限流效果）")
    @GetMapping("/stress")
    public Result<String> stress() {
        long start = System.currentTimeMillis();
        int success = 0, blocked = 0;
        for (int i = 0; i < 20; i++) {
            try {
                rateLimitService.distributedLimited();
                success++;
            } catch (Exception e) {
                blocked++;
            }
        }
        return Result.success("success=" + success + ", blocked=" + blocked + ", time=" + (System.currentTimeMillis() - start) + "ms");
    }
}
