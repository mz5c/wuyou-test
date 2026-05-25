package com.wuyou.onlytest.redis;

import com.wuyou.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "Redis 数据结构测试")
@RestController
@RequestMapping("/api/v1/redis/ds")
@RequiredArgsConstructor
public class RedisDataStructureController {

    private final RedisDataStructureService service;

    // ==================== List ====================

    @Operation(summary = "List - 从右侧推入元素 (RPUSH)")
    @PostMapping("/list/rpush")
    public Result<Long> listRpush(@RequestParam String key, @RequestParam String value) {
        return Result.success(service.listPush(key, value));
    }

    @Operation(summary = "List - 从左侧推入元素 (LPUSH)")
    @PostMapping("/list/lpush")
    public Result<Long> listLpush(@RequestParam String key, @RequestParam String value) {
        return Result.success(service.listLeftPush(key, value));
    }

    @Operation(summary = "List - 从右侧弹出元素 (RPOP)")
    @PostMapping("/list/rpop")
    public Result<Object> listRpop(@RequestParam String key) {
        return Result.success(service.listRightPop(key));
    }

    @Operation(summary = "List - 从左侧弹出元素 (LPOP)")
    @PostMapping("/list/lpop")
    public Result<Object> listLpop(@RequestParam String key) {
        return Result.success(service.listLeftPop(key));
    }

    @Operation(summary = "List - 获取指定范围元素 (LRANGE)")
    @GetMapping("/list/range")
    public Result<List<Object>> listRange(@RequestParam String key,
                                          @RequestParam(defaultValue = "0") long start,
                                          @RequestParam(defaultValue = "-1") long end) {
        return Result.success(service.listRange(key, start, end));
    }

    @Operation(summary = "List - 获取列表长度 (LLEN)")
    @GetMapping("/list/size")
    public Result<Long> listSize(@RequestParam String key) {
        return Result.success(service.listSize(key));
    }

    @Operation(summary = "List - 设置指定索引的值 (LSET)")
    @PostMapping("/list/set")
    public Result<Void> listSet(@RequestParam String key,
                                @RequestParam long index,
                                @RequestParam String value) {
        service.listSet(key, index, value);
        return Result.success(null);
    }

    @Operation(summary = "List - 删除指定元素 (LREM)")
    @PostMapping("/list/remove")
    public Result<Long> listRemove(@RequestParam String key,
                                   @RequestParam(defaultValue = "1") long count,
                                   @RequestParam String value) {
        return Result.success(service.listRemove(key, count, value));
    }

    // ==================== Hash ====================

    @Operation(summary = "Hash - 设置字段值 (HSET)")
    @PostMapping("/hash/put")
    public Result<Void> hashPut(@RequestParam String key,
                                @RequestParam String field,
                                @RequestParam String value) {
        service.hashPut(key, field, value);
        return Result.success(null);
    }

    @Operation(summary = "Hash - 批量设置 (HMSET)")
    @PostMapping("/hash/put-all")
    public Result<Void> hashPutAll(@RequestParam String key,
                                   @RequestBody Map<String, Object> map) {
        service.hashPutAll(key, map);
        return Result.success(null);
    }

    @Operation(summary = "Hash - 获取字段值 (HGET)")
    @GetMapping("/hash/get")
    public Result<Object> hashGet(@RequestParam String key, @RequestParam String field) {
        return Result.success(service.hashGet(key, field));
    }

    @Operation(summary = "Hash - 获取所有字段和值 (HGETALL)")
    @GetMapping("/hash/entries")
    public Result<Map<Object, Object>> hashEntries(@RequestParam String key) {
        return Result.success(service.hashEntries(key));
    }

    @Operation(summary = "Hash - 获取所有字段名 (HKEYS)")
    @GetMapping("/hash/keys")
    public Result<Set<Object>> hashKeys(@RequestParam String key) {
        return Result.success(service.hashKeys(key));
    }

    @Operation(summary = "Hash - 获取所有字段值 (HVALS)")
    @GetMapping("/hash/values")
    public Result<List<Object>> hashValues(@RequestParam String key) {
        return Result.success(service.hashValues(key));
    }

    @Operation(summary = "Hash - 判断字段是否存在 (HEXISTS)")
    @GetMapping("/hash/exists")
    public Result<Boolean> hashExists(@RequestParam String key, @RequestParam String field) {
        return Result.success(service.hashHasKey(key, field));
    }

    @Operation(summary = "Hash - 删除字段 (HDEL)")
    @PostMapping("/hash/delete")
    public Result<Long> hashDelete(@RequestParam String key, @RequestParam String... fields) {
        return Result.success(service.hashDelete(key, fields));
    }

    @Operation(summary = "Hash - 获取字段数量 (HLEN)")
    @GetMapping("/hash/size")
    public Result<Long> hashSize(@RequestParam String key) {
        return Result.success(service.hashSize(key));
    }

    // ==================== Set ====================

    @Operation(summary = "Set - 添加元素 (SADD)")
    @PostMapping("/set/add")
    public Result<Long> setAdd(@RequestParam String key, @RequestParam String... values) {
        return Result.success(service.setAdd(key, values));
    }

    @Operation(summary = "Set - 获取所有元素 (SMEMBERS)")
    @GetMapping("/set/members")
    public Result<Set<Object>> setMembers(@RequestParam String key) {
        return Result.success(service.setMembers(key));
    }

    @Operation(summary = "Set - 判断元素是否存在 (SISMEMBER)")
    @GetMapping("/set/contains")
    public Result<Boolean> setContains(@RequestParam String key, @RequestParam String value) {
        return Result.success(service.setIsMember(key, value));
    }

    @Operation(summary = "Set - 获取元素个数 (SCARD)")
    @GetMapping("/set/size")
    public Result<Long> setSize(@RequestParam String key) {
        return Result.success(service.setSize(key));
    }

    @Operation(summary = "Set - 删除元素 (SREM)")
    @PostMapping("/set/remove")
    public Result<Long> setRemove(@RequestParam String key, @RequestParam String... values) {
        return Result.success(service.setRemove(key, values));
    }

    @Operation(summary = "Set - 交集 (SINTER)")
    @GetMapping("/set/intersect")
    public Result<Set<Object>> setIntersect(@RequestParam String key1, @RequestParam String key2) {
        return Result.success(service.setIntersect(key1, key2));
    }

    @Operation(summary = "Set - 并集 (SUNION)")
    @GetMapping("/set/union")
    public Result<Set<Object>> setUnion(@RequestParam String key1, @RequestParam String key2) {
        return Result.success(service.setUnion(key1, key2));
    }

    @Operation(summary = "Set - 差集 (SDIFF)")
    @GetMapping("/set/diff")
    public Result<Set<Object>> setDifference(@RequestParam String key1, @RequestParam String key2) {
        return Result.success(service.setDifference(key1, key2));
    }

    @Operation(summary = "Set - 随机获取一个元素 (SRANDMEMBER)")
    @GetMapping("/set/random-member")
    public Result<Object> setRandomMember(@RequestParam String key) {
        return Result.success(service.setRandomMember(key));
    }

    @Operation(summary = "Set - 随机弹出一个元素 (SPOP)")
    @PostMapping("/set/pop")
    public Result<Object> setPop(@RequestParam String key) {
        return Result.success(service.setPop(key));
    }

    // ==================== ZSet ====================

    @Operation(summary = "ZSet - 添加元素 (ZADD)")
    @PostMapping("/zset/add")
    public Result<Boolean> zsetAdd(@RequestParam String key,
                                   @RequestParam String value,
                                   @RequestParam double score) {
        return Result.success(service.zsetAdd(key, value, score));
    }

    @Operation(summary = "ZSet - 批量添加")
    @PostMapping("/zset/add-batch")
    public Result<Long> zsetAddBatch(@RequestParam String key,
                                     @RequestBody Map<String, Double> members) {
        return Result.success(service.zsetAddBatch(key, members));
    }

    @Operation(summary = "ZSet - 获取元素分数 (ZSCORE)")
    @GetMapping("/zset/score")
    public Result<Double> zsetScore(@RequestParam String key, @RequestParam String value) {
        return Result.success(service.zsetScore(key, value));
    }

    @Operation(summary = "ZSet - 获取正序排名 (ZRANK)")
    @GetMapping("/zset/rank")
    public Result<Long> zsetRank(@RequestParam String key, @RequestParam String value) {
        return Result.success(service.zsetRank(key, value));
    }

    @Operation(summary = "ZSet - 获取倒序排名 (ZREVRANK)")
    @GetMapping("/zset/reverse-rank")
    public Result<Long> zsetReverseRank(@RequestParam String key, @RequestParam String value) {
        return Result.success(service.zsetReverseRank(key, value));
    }

    @Operation(summary = "ZSet - 按索引范围获取 (ZRANGE)")
    @GetMapping("/zset/range")
    public Result<Set<Object>> zsetRange(@RequestParam String key,
                                         @RequestParam(defaultValue = "0") long start,
                                         @RequestParam(defaultValue = "-1") long end) {
        return Result.success(service.zsetRange(key, start, end));
    }

    @Operation(summary = "ZSet - 按索引范围倒序获取 (ZREVRANGE)")
    @GetMapping("/zset/reverse-range")
    public Result<Set<Object>> zsetReverseRange(@RequestParam String key,
                                                @RequestParam(defaultValue = "0") long start,
                                                @RequestParam(defaultValue = "-1") long end) {
        return Result.success(service.zsetReverseRange(key, start, end));
    }

    @Operation(summary = "ZSet - 按分数范围获取 (ZRANGEBYSCORE)")
    @GetMapping("/zset/range-by-score")
    public Result<Set<Object>> zsetRangeByScore(@RequestParam String key,
                                                @RequestParam(defaultValue = "0") double min,
                                                @RequestParam(defaultValue = "100") double max) {
        return Result.success(service.zsetRangeByScore(key, min, max));
    }

    @Operation(summary = "ZSet - 获取元素个数 (ZCARD)")
    @GetMapping("/zset/size")
    public Result<Long> zsetSize(@RequestParam String key) {
        return Result.success(service.zsetSize(key));
    }

    @Operation(summary = "ZSet - 统计分数范围内元素个数 (ZCOUNT)")
    @GetMapping("/zset/count")
    public Result<Long> zsetCount(@RequestParam String key,
                                  @RequestParam(defaultValue = "0") double min,
                                  @RequestParam(defaultValue = "100") double max) {
        return Result.success(service.zsetCount(key, min, max));
    }

    @Operation(summary = "ZSet - 增加分数 (ZINCRBY)")
    @PostMapping("/zset/increment-score")
    public Result<Double> zsetIncrementScore(@RequestParam String key,
                                             @RequestParam String value,
                                             @RequestParam double delta) {
        return Result.success(service.zsetIncrementScore(key, value, delta));
    }

    @Operation(summary = "ZSet - 删除元素 (ZREM)")
    @PostMapping("/zset/remove")
    public Result<Long> zsetRemove(@RequestParam String key, @RequestParam String... values) {
        return Result.success(service.zsetRemove(key, values));
    }

    @Operation(summary = "ZSet - 按索引范围删除 (ZREMRANGEBYRANK)")
    @PostMapping("/zset/remove-range")
    public Result<Void> zsetRemoveRange(@RequestParam String key,
                                        @RequestParam long start,
                                        @RequestParam long end) {
        service.zsetRemoveRange(key, start, end);
        return Result.success(null);
    }
}
