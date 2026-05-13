package com.wuyou.onlytest.localcache.controller;

import com.wuyou.onlytest.localcache.service.CaffeineCacheService;
import com.wuyou.onlytest.localcache.service.GuavaCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/local-cache")
@RequiredArgsConstructor
public class LocalCacheController {

    private final GuavaCacheService guavaCacheService;
    private final CaffeineCacheService caffeineCacheService;

    // ==================== Guava Cache ====================

    @GetMapping("/guava/auto-load")
    public String guavaAutoLoad(@RequestParam String key) throws ExecutionException {
        return guavaCacheService.getAutoLoad(key);
    }

    @GetMapping("/guava/write-expire")
    public String guavaWriteExpire(@RequestParam String key) {
        return guavaCacheService.getWithWriteExpire(key);
    }

    @GetMapping("/guava/access-expire")
    public String guavaAccessExpire(@RequestParam String key) {
        return guavaCacheService.getWithAccessExpire(key);
    }

    @GetMapping("/guava/stats")
    public Map<String, Object> guavaStats() {
        return guavaCacheService.getStatsCache();
    }

    @PostMapping("/guava/removal-test")
    public String guavaRemovalTest() {
        for (int i = 0; i < 10; i++) {
            guavaCacheService.putToRemovalCache("k" + i, "v" + i);
        }
        return "removalCount=" + guavaCacheService.getRemovalCount()
                + ", size=" + guavaCacheService.getRemovalCacheSize();
    }

    // ==================== Caffeine ====================

    @GetMapping("/caffeine/basic")
    public String caffeineBasic(@RequestParam String key) {
        return caffeineCacheService.getBasic(key);
    }

    @GetMapping("/caffeine/loading")
    public String caffeineLoading(@RequestParam String key) {
        return caffeineCacheService.getLoading(key);
    }

    @GetMapping("/caffeine/refresh")
    public String caffeineRefresh(@RequestParam String key) {
        return caffeineCacheService.getRefresh(key);
    }

    @GetMapping("/caffeine/async")
    public String caffeineAsync(@RequestParam String key) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = caffeineCacheService.getAsync(key);
        return future.get();
    }

    @GetMapping("/caffeine/custom-expire")
    public String caffeineCustomExpire(@RequestParam String key) {
        return caffeineCacheService.getCustomExpire(key);
    }

    @GetMapping("/caffeine/stats")
    public Map<String, Object> caffeineStats() {
        return caffeineCacheService.getDetailedStats();
    }

    @GetMapping("/caffeine/batch")
    public Map<String, String> caffeineBatch(@RequestParam Set<String> keys) {
        return caffeineCacheService.getAll(keys);
    }

    @PostMapping("/caffeine/eviction-test")
    public String caffeineEvictionTest() {
        for (int i = 0; i < 10; i++) {
            caffeineCacheService.putToMonitor("k" + i, "v" + i);
        }
        return "evictionCount=" + caffeineCacheService.getMonitorEvictionCount()
                + ", size=" + caffeineCacheService.getMonitorCacheSize();
    }

    @PostMapping("/invalidate")
    public String invalidate(@RequestParam String key) {
        guavaCacheService.invalidate(key);
        caffeineCacheService.invalidate(key);
        return "invalidated: " + key;
    }
}
