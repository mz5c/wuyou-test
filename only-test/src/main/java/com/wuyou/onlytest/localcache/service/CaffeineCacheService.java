package com.wuyou.onlytest.localcache.service;

import com.github.benmanes.caffeine.cache.*;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class CaffeineCacheService {

    // ========== 1. 基本 Cache — 手动 get/put ==========

    private final Cache<String, String> basicCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .recordStats()
            .build();

    // ========== 2. LoadingCache — 自动加载 ==========

    private final LoadingCache<String, String> loadingCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .recordStats()
            .build(key -> {
                log.info("[Caffeine] LoadingCache 自动加载 key={}", key);
                return "loaded:" + key;
            });

    // ========== 3. 定时刷新 — refreshAfterWrite ==========

    /** 写入 3s 后访问触发异步刷新，刷新期间返回旧值 */
    private final LoadingCache<String, String> refreshCache = Caffeine.newBuilder()
            .maximumSize(100)
            .refreshAfterWrite(3, TimeUnit.SECONDS)
            .recordStats()
            .build(key -> {
                log.info("[Caffeine] 异步刷新 key={}", key);
                // 模拟耗时加载
                try { TimeUnit.MILLISECONDS.sleep(200); } catch (InterruptedException ignored) { }
                return "refreshed:" + key + ":" + System.currentTimeMillis();
            });

    // ========== 4. 基于权重的淘汰 ==========

    private final LoadingCache<String, String> weightCache = Caffeine.newBuilder()
            .maximumWeight(100)
            .weigher((String key, String value) -> key.length() + value.length())
            .recordStats()
            .build(key -> "weight:" + key);

    // ========== 5. AsyncLoadingCache — 异步加载 ==========

    private final AsyncLoadingCache<String, String> asyncCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .buildAsync(key -> {
                log.info("[Caffeine] 异步加载 key={}", key);
                TimeUnit.MILLISECONDS.sleep(200);
                return "async:" + key;
            });

    // ========== 6. 自定义过期策略 ==========

    private final Cache<String, String> customExpireCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfter(new Expiry<String, String>() {
                @Override
                public long expireAfterCreate(String key, String value, long currentTime) {
                    // 不同 key 不同过期时间
                    if (key.startsWith("short")) {
                        return TimeUnit.SECONDS.toNanos(2);
                    }
                    return TimeUnit.SECONDS.toNanos(10);
                }

                @Override
                public long expireAfterUpdate(String key, String value, long currentTime, long currentDuration) {
                    return currentDuration;
                }

                @Override
                public long expireAfterRead(String key, String value, long currentTime, long currentDuration) {
                    return currentDuration;
                }
            })
            .build();

    // ========== 7. 统计 + 淘汰监听 ==========

    private final AtomicLong evictionCount = new AtomicLong(0);

    private final Cache<String, String> monitorCache = Caffeine.newBuilder()
            .maximumSize(3)
            .evictionListener((String key, String value, RemovalCause cause) -> {
                evictionCount.incrementAndGet();
                log.info("[Caffeine 淘汰] key={}, value={}, cause={}", key, value, cause);
            })
            .recordStats()
            .build();

    // ========================================================================

    public String getBasic(String key) {
        return basicCache.get(key, k -> "basic:" + k);
    }

    public String getLoading(String key) {
        return loadingCache.get(key);
    }

    public String getRefresh(String key) {
        return refreshCache.get(key);
    }

    public String getWeight(String key) {
        return weightCache.get(key);
    }

    public CompletableFuture<String> getAsync(String key) {
        return asyncCache.get(key);
    }

    public String getCustomExpire(String key) {
        return customExpireCache.get(key, k -> "custom:" + k + ":" + System.currentTimeMillis());
    }

    /** 手动 put */
    public String putAndGet(String key, String value) {
        basicCache.put(key, value);
        return basicCache.getIfPresent(key);
    }

    /** 批量获取 */
    public Map<String, String> getAll(Set<String> keys) {
        return loadingCache.getAll(keys);
    }

    /** 失效 key */
    public void invalidate(String key) {
        basicCache.invalidate(key);
        loadingCache.invalidate(key);
    }

    /** 失效所有 */
    public void invalidateAll() {
        basicCache.invalidateAll();
        loadingCache.invalidateAll();
    }

    /** 当前缓存大小 */
    public long estimatedSize() {
        return basicCache.estimatedSize();
    }

    /** 统计信息 */
    public CacheStats getStats() {
        return basicCache.stats();
    }

    /** 监控缓存 — 插入超过 maxSize 触发淘汰监听 */
    public void putToMonitor(String key, String value) {
        monitorCache.put(key, value);
    }

    /** 强制触发缓存维护（淘汰等） */
    public void cleanUpMonitor() {
        monitorCache.cleanUp();
    }

    public long getMonitorEvictionCount() {
        return evictionCount.get();
    }

    public long getMonitorCacheSize() {
        return monitorCache.estimatedSize();
    }

    /** 详细的统计报告 */
    public Map<String, Object> getDetailedStats() {
        CacheStats stats = basicCache.stats();
        return Map.ofEntries(
                Map.entry("hitCount", stats.hitCount()),
                Map.entry("missCount", stats.missCount()),
                Map.entry("hitRate", stats.hitRate()),
                Map.entry("missRate", stats.missRate()),
                Map.entry("evictionCount", stats.evictionCount()),
                Map.entry("evictionWeight", stats.evictionWeight()),
                Map.entry("loadCount", stats.loadCount()),
                Map.entry("loadFailureCount", stats.loadFailureCount()),
                Map.entry("totalLoadTime", stats.totalLoadTime()),
                Map.entry("averageLoadPenalty", stats.averageLoadPenalty()),
                Map.entry("requestCount", stats.requestCount())
        );
    }

    @PostConstruct
    public void init() {
        log.info("[CaffeineCacheService] 初始化完成");
    }

    @PreDestroy
    public void cleanup() {
        log.info("[CaffeineCacheService] 清理");
        invalidateAll();
    }
}
