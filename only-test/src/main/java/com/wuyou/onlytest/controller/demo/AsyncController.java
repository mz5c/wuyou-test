package com.wuyou.onlytest.controller.demo;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.service.demo.AsyncTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "异步任务", description = "异步任务处理接口")
@RestController
@RequestMapping("/api/v1/async")
@RequiredArgsConstructor
public class AsyncController {

    private final AsyncTaskService asyncTaskService;

    @Operation(summary = "发送异步通知")
    @PostMapping("/notification")
    public Result<Void> sendNotification(@Parameter(description = "用户 ID", required = true) @RequestParam Long userId, 
                                         @Parameter(description = "通知消息", required = true) @RequestParam String message) {
        asyncTaskService.sendNotification(userId, message);
        return Result.success(null);
    }
}
