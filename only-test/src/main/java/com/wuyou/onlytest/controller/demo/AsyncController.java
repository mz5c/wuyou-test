package com.wuyou.onlytest.controller.demo;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.service.demo.AsyncTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/async")
@RequiredArgsConstructor
public class AsyncController {

    private final AsyncTaskService asyncTaskService;

    @PostMapping("/notification")
    public Result<Void> sendNotification(@RequestParam Long userId, @RequestParam String message) {
        asyncTaskService.sendNotification(userId, message);
        return Result.success(null);
    }
}
