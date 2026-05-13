package com.wuyou.onlytest.localcache.service;

import com.google.common.cache.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class GuavaCacheService {

    // ========== 1. LoadingCache — 自动加载 ==========

    private final LoadingCache<String, String> autoLoadCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .recordStats()
            .removalListener((RemovalNotification<String, String> notification) ->
                    log.info("[Guava] 淘汰 key={}, value={}, cause={}", notification.getKey(), notification.getValue(), notification.getCause()))
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String key) {
                    log.info("[Guava] 自动加载 key={}", key);
                    return "loaded:" + key;
                }
            });

    // ========== 2. Callable 缓存 — 手动控制加载 ==========

    private final Cache<String, String> callableCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .recordStats()
            .build();

    // ========== 3. 过期缓存 — expireAfterWrite vs expireAfterAccess ==========

    /** expireAfterWrite: 写入后固定时间过期 */
    private final Cache<String, String> writeExpireCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .maximumSize(1000)
            .build();

    /** expireAfterAccess: 最后一次访问后固定时间过期（读/写都重置计时） */
    private final Cache<String, String> accessExpireCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .maximumSize(1000)
            .build();

    // ========== 4. 软引用缓存 — 内存敏感 ==========

    private final Cache<String, String> softValuesCache = CacheBuilder.newBuilder()
            .softValues()
            .build();

    // ========== 5. 统计信息缓存 ==========

    private final LoadingCache<String, String> statsCache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .recordStats()
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String key) {
                    return "stats:" + key;
                }
            });

    // ========== 6. 移除监听器演示 ==========

    private final AtomicInteger removalCount = new AtomicInteger(0);

    private final Cache<String, String> removalCache = CacheBuilder.newBuilder()
            .maximumSize(5)
            .removalListener((RemovalNotification<String, String> notification) -> {
                removalCount.incrementAndGet();
                log.info("[Guava RemovalListener] key={}, value={}, cause={}",
                        notification.getKey(), notification.getValue(), notification.getCause());
            })
            .build();

    // ========================================================================

    /** 获取自动加载缓存的值 */
    public String getAutoLoad(String key) throws ExecutionException {
        return autoLoadCache.get(key);
    }

    /** callable 方式获取/加载 */
    public String getWithCallable(String key, Callable<String> loader) throws ExecutionException {
        return callableCache.get(key, loader);
    }

    /** 写过期缓存 — 写入后 5s 过期 */
    public String getWithWriteExpire(String key) {
        try {
            return writeExpireCache.get(key, () -> "writeExpire:" + key + ":" + System.currentTimeMillis());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /** 访问过期缓存 — 5s 无访问则过期 */
    public String getWithAccessExpire(String key) {
        try {
            return accessExpireCache.get(key, () -> "accessExpire:" + key + ":" + System.currentTimeMillis());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /** 软引用缓存 — JVM GC 时可回收 */
    public String getSoftValue(String key) {
        String val = softValuesCache.getIfPresent(key);
        if (val == null) {
            val = "soft:" + key;
            softValuesCache.put(key, val);
        }
        return val;
    }

    /** 手动 put / get / invalidate */
    public String manualPutAndGet(String key, String value) {
        callableCache.put(key, value);
        return callableCache.getIfPresent(key);
    }

    /** 失效指定 key */
    public void invalidate(String key) {
        autoLoadCache.invalidate(key);
        callableCache.invalidate(key);
    }

    /** 失效所有 */
    public void invalidateAll() {
        autoLoadCache.invalidateAll();
        callableCache.invalidateAll();
    }

    /** 获取统计信息 */
    public CacheStats getAutoLoadStats() {
        return autoLoadCache.stats();
    }

    /** 统计缓存命中情况 */
    public Map<String, Object> getStatsCache() {
        statsCache.getUnchecked("a");
        statsCache.getUnchecked("a");
        statsCache.getUnchecked("b");
        statsCache.getUnchecked("c");

        ConcurrentMap<String, String> asMap = statsCache.asMap();
        asMap.get("d"); // miss

        CacheStats stats = statsCache.stats();
        return Map.of(
                "hitCount", stats.hitCount(),
                "missCount", stats.missCount(),
                "hitRate", stats.hitRate(),
                "evictionCount", stats.evictionCount(),
                "loadCount", stats.loadCount(),
                "totalLoadTime", stats.totalLoadTime()
        );
    }

    /** 移除监听缓存 — 插入超过 maxSize 触发淘汰 */
    public void putToRemovalCache(String key, String value) {
        removalCache.put(key, value);
    }

    public int getRemovalCount() {
        return removalCount.get();
    }

    public long getRemovalCacheSize() {
        return removalCache.size();
    }

    @PostConstruct
    public void init() {
        log.info("[GuavaCacheService] 初始化完成");
    }

    @PreDestroy
    public void cleanup() {
        log.info("[GuavaCacheService] 清理");
        invalidateAll();
    }
}
