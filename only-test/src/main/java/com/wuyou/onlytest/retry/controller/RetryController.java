package com.wuyou.onlytest.retry.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.retry.service.RetryDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Spring Retry 重试测试")
@RestController
@RequestMapping("/api/v1/retry")
@RequiredArgsConstructor
public class RetryController {

    private final RetryDemoService retryDemoService;

    @Operation(summary = "不稳定调用 — 重试 3 次，间隔递增")
    @PostMapping("/unstable")
    public Result<String> unstable(@RequestParam(defaultValue = "true") boolean shouldFail) {
        retryDemoService.resetCounter();
        return Result.success(retryDemoService.unstableCall(shouldFail));
    }

    @Operation(summary = "重试耗尽后回退")
    @PostMapping("/fallback")
    public Result<String> fallback() {
        retryDemoService.resetCounter();
        return Result.success(retryDemoService.unstableCall(true));
    }
}
