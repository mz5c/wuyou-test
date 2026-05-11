package com.wuyou.onlytest.lock.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.entity.demo.Product;
import com.wuyou.onlytest.lock.service.LockDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Redisson 锁测试")
@RestController
@RequestMapping("/api/v1/locks")
@RequiredArgsConstructor
public class LockController {

    private final LockDemoService lockDemoService;

    @Operation(summary = "获取读锁查询商品")
    @PostMapping("/product/{id}/read")
    public Result<Product> readProduct(@PathVariable Long id) {
        return Result.success(lockDemoService.readProduct(id));
    }

    @Operation(summary = "获取写锁更新商品")
    @PostMapping("/product/{id}/write")
    public Result<Void> writeProduct(@PathVariable Long id, @RequestParam int stock) {
        lockDemoService.writeProduct(id, stock);
        return Result.success(null);
    }

    @Operation(summary = "获取信号量")
    @PostMapping("/semaphore/acquire")
    public Result<Boolean> acquireSemaphore(@RequestParam String name, @RequestParam(defaultValue = "1") int permits) {
        return Result.success(lockDemoService.acquireSemaphore(name, permits));
    }

    @Operation(summary = "释放信号量")
    @PostMapping("/semaphore/release")
    public Result<Void> releaseSemaphore(@RequestParam String name, @RequestParam(defaultValue = "1") int permits) {
        lockDemoService.releaseSemaphore(name, permits);
        return Result.success(null);
    }
}
