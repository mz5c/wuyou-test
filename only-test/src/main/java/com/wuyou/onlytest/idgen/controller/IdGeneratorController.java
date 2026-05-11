package com.wuyou.onlytest.idgen.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.idgen.service.RedisIdGenService;
import com.wuyou.onlytest.idgen.service.SnowflakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "分布式 ID 生成")
@RestController
@RequestMapping("/api/v1/idgen")
@RequiredArgsConstructor
public class IdGeneratorController {

    private final SnowflakeService snowflakeService;
    private final RedisIdGenService redisIdGenService;

    @Operation(summary = "雪花算法 ID")
    @GetMapping("/snowflake")
    public Result<Long> snowflake() {
        return Result.success(snowflakeService.nextId());
    }

    @Operation(summary = "Redis 自增 ID")
    @GetMapping("/redis/{bizType}")
    public Result<Long> redisId(@PathVariable String bizType) {
        return Result.success(redisIdGenService.nextId(bizType));
    }

    @Operation(summary = "批量生成对比性能")
    @GetMapping("/compare")
    public Result<Map<String, List<Long>>> compare(@RequestParam(defaultValue = "1000") int count) {
        List<Long> snowflakeIds = snowflakeService.batchNextId(count);
        List<Long> redisIds = redisIdGenService.batchNextId("compare", count);
        return Result.success(Map.of("snowflake", snowflakeIds, "redis", redisIds));
    }
}
