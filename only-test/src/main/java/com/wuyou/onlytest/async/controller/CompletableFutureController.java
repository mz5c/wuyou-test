package com.wuyou.onlytest.async.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.async.service.CompletableFutureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Tag(name = "CompletableFuture 并发编排")
@RestController
@RequestMapping("/api/v1/async")
@RequiredArgsConstructor
public class CompletableFutureController {

    private final CompletableFutureService completableFutureService;

    @Operation(summary = "并行查询 — 同时查用户、订单、推荐")
    @GetMapping("/parallel")
    public Result<List<String>> parallel(@RequestParam Long userId) throws Exception {
        CompletableFuture<String> userF = completableFutureService.queryUserInfo(userId);
        CompletableFuture<String> orderF = completableFutureService.queryOrderList(userId);
        CompletableFuture<String> recF = completableFutureService.queryRecommendations(userId);

        CompletableFuture<Void> all = CompletableFuture.allOf(userF, orderF, recF);
        all.get();

        return Result.success(List.of(userF.get(), orderF.get(), recF.get()));
    }

    @Operation(summary = "竞速模式 — 多渠道通知，谁快取谁")
    @PostMapping("/race")
    public Result<String> race(@RequestParam Long userId) {
        CompletableFuture<String> sms = completableFutureService.notifyChannel(userId, "SMS");
        CompletableFuture<String> email = completableFutureService.notifyChannel(userId, "EMAIL");
        CompletableFuture<String> siteMsg = completableFutureService.notifyChannel(userId, "SITE_MSG");

        return Result.success(CompletableFuture.anyOf(sms, email, siteMsg)
                .thenApply(Object::toString).join());
    }

    @Operation(summary = "异常兜底 — 一个失败返回降级结果")
    @GetMapping("/error-handling")
    public Result<String> errorHandling(@RequestParam(defaultValue = "false") boolean shouldFail) {
        CompletableFuture<String> future = completableFutureService.stepWithFailure(1L, shouldFail);
        String result = future.exceptionally(ex -> "FALLBACK:" + ex.getMessage()).join();
        return Result.success(result);
    }
}
