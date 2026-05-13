package com.wuyou.onlytest.localcache;

import com.wuyou.onlytest.localcache.service.GuavaCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GuavaCacheTest {

    @Autowired
    private GuavaCacheService guavaCacheService;

    @BeforeEach
    void setUp() {
        guavaCacheService.invalidateAll();
    }

    @Test
    void testAutoLoadCache() throws ExecutionException {
        String result = guavaCacheService.getAutoLoad("hello");
        assertThat(result).isEqualTo("loaded:hello");

        // 再次获取，命中缓存
        String cached = guavaCacheService.getAutoLoad("hello");
        assertThat(cached).isEqualTo("loaded:hello");
    }

    @Test
    void testCallableCache() throws ExecutionException {
        String result = guavaCacheService.getWithCallable("key1", () -> "computed");
        assertThat(result).isEqualTo("computed");

        // 命中缓存
        String cached = guavaCacheService.getWithCallable("key1", () -> "should-not-call");
        assertThat(cached).isEqualTo("computed");
    }

    @Test
    void testManualPutAndGet() {
        String result = guavaCacheService.manualPutAndGet("manual", "manual-value");
        assertThat(result).isEqualTo("manual-value");
    }

    @Test
    void testWriteExpire() throws InterruptedException {
        String v1 = guavaCacheService.getWithWriteExpire("wkey");
        String v2 = guavaCacheService.getWithWriteExpire("wkey");
        assertThat(v1).isEqualTo(v2);

        // 等待过期
        TimeUnit.SECONDS.sleep(6);

        String v3 = guavaCacheService.getWithWriteExpire("wkey");
        assertThat(v3).isNotEqualTo(v1);
    }

    @Test
    void testInvalidate() throws ExecutionException {
        guavaCacheService.getAutoLoad("inv");
        guavaCacheService.invalidate("inv");
        // 失效后重新加载
        String result = guavaCacheService.getAutoLoad("inv");
        assertThat(result).isEqualTo("loaded:inv");
    }

    @Test
    void testStats() {
        var stats = guavaCacheService.getStatsCache();
        assertThat(stats)
                .containsKey("hitCount")
                .containsKey("missCount")
                .containsKey("hitRate");
    }

    @Test
    void testRemovalListener() {
        // 插入 10 个，最大容量 5，应淘汰 5 个
        for (int i = 0; i < 10; i++) {
            guavaCacheService.putToRemovalCache("k" + i, "v" + i);
        }
        assertThat(guavaCacheService.getRemovalCount()).isPositive();
        assertThat(guavaCacheService.getRemovalCacheSize()).isLessThanOrEqualTo(5);
    }
}
