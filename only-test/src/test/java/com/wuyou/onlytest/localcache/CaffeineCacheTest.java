package com.wuyou.onlytest.localcache;

import com.wuyou.onlytest.localcache.service.CaffeineCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CaffeineCacheTest {

    @Autowired
    private CaffeineCacheService caffeineCacheService;

    @BeforeEach
    void setUp() {
        caffeineCacheService.invalidateAll();
    }

    @Test
    void testBasicCache() {
        String result = caffeineCacheService.getBasic("hello");
        assertThat(result).isEqualTo("basic:hello");

        // 命中缓存
        String cached = caffeineCacheService.getBasic("hello");
        assertThat(cached).isEqualTo("basic:hello");
    }

    @Test
    void testPutAndGet() {
        String result = caffeineCacheService.putAndGet("manual", "manual-value");
        assertThat(result).isEqualTo("manual-value");
    }

    @Test
    void testLoadingCache() {
        String result = caffeineCacheService.getLoading("key1");
        assertThat(result).isEqualTo("loaded:key1");

        // 命中
        String cached = caffeineCacheService.getLoading("key1");
        assertThat(cached).isEqualTo("loaded:key1");
    }

    @Test
    void testBatchGet() {
        Map<String, String> all = caffeineCacheService.getAll(Set.of("a", "b", "c"));
        assertThat(all).hasSize(3);
        assertThat(all).containsEntry("a", "loaded:a");
        assertThat(all).containsEntry("b", "loaded:b");
    }

    @Test
    void testRefreshAfterWrite() throws InterruptedException {
        String v1 = caffeineCacheService.getRefresh("rk");
        // 等待定时刷新
        TimeUnit.SECONDS.sleep(4);

        String v2 = caffeineCacheService.getRefresh("rk");
        // refreshAfterWrite 在访问时触发异步刷新，但返回旧值
        // 这里只验证不抛异常
        assertThat(v2).isNotNull();
    }

    @Test
    void testWeightCache() {
        String result = caffeineCacheService.getWeight("wk");
        assertThat(result).isEqualTo("weight:wk");
    }

    @Test
    void testAsyncCache() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = caffeineCacheService.getAsync("ak");
        String result = future.get();
        assertThat(result).isEqualTo("async:ak");

        // 再次异步获取，命中缓存
        String cached = caffeineCacheService.getAsync("ak").get();
        assertThat(cached).isEqualTo("async:ak");
    }

    @Test
    void testCustomExpire() throws InterruptedException {
        // short 前缀的 key 2s 过期
        String v1 = caffeineCacheService.getCustomExpire("short-k1");
        TimeUnit.SECONDS.sleep(3);

        String v2 = caffeineCacheService.getCustomExpire("short-k1");
        assertThat(v2).isNotEqualTo(v1)
                .as("自定义过期时间短 key 应已过期");
    }

    @Test
    void testInvalidate() {
        caffeineCacheService.getBasic("inv");
        caffeineCacheService.invalidate("inv");
        // 失效后应重新加载
        String result = caffeineCacheService.getBasic("inv");
        assertThat(result).isEqualTo("basic:inv");
    }

    @Test
    void testEvictionListener() {
        // 插入 10 个，最大容量 3，应淘汰 7 个
        for (int i = 0; i < 10; i++) {
            caffeineCacheService.putToMonitor("k" + i, "v" + i);
        }
        // 触发缓存维护，确保淘汰事件已处理
        caffeineCacheService.cleanUpMonitor();
        assertThat(caffeineCacheService.getMonitorEvictionCount()).isPositive();
        assertThat(caffeineCacheService.getMonitorCacheSize()).isLessThanOrEqualTo(3);
    }

    @Test
    void testStats() {
        Map<String, Object> stats = caffeineCacheService.getDetailedStats();
        assertThat(stats)
                .containsKey("hitCount")
                .containsKey("missCount")
                .containsKey("hitRate")
                .containsKey("requestCount")
                .containsKey("averageLoadPenalty");
    }

    @Test
    void testEstimatedSize() {
        caffeineCacheService.getBasic("s1");
        caffeineCacheService.getBasic("s2");
        caffeineCacheService.getBasic("s3");
        assertThat(caffeineCacheService.estimatedSize()).isPositive();
    }
}
