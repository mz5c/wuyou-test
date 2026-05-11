package com.wuyou.onlytest.async.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class CompletableFutureService {

    @Async
    public CompletableFuture<String> queryUserInfo(Long userId) {
        sleep(1500);
        log.info("user info query done: {}", userId);
        return CompletableFuture.completedFuture("USER:" + userId + ":张三");
    }

    @Async
    public CompletableFuture<String> queryOrderList(Long userId) {
        sleep(2000);
        log.info("order list query done: {}", userId);
        return CompletableFuture.completedFuture("ORDERS:ORD2024001,ORD2024002");
    }

    @Async
    public CompletableFuture<String> queryRecommendations(Long userId) {
        sleep(1000);
        log.info("recommendations query done: {}", userId);
        return CompletableFuture.completedFuture("RECOMMEND:iPhone,MacBook");
    }

    @Async
    public CompletableFuture<String> notifyChannel(Long userId, String channel) {
        sleep(500);
        log.info("notify {} done: {}", channel, userId);
        return CompletableFuture.completedFuture(channel + "_SENT:" + userId);
    }

    @Async
    public CompletableFuture<String> stepWithFailure(Long id, boolean shouldFail) {
        sleep(800);
        if (shouldFail) {
            return CompletableFuture.failedFuture(new RuntimeException("step failed for id=" + id));
        }
        return CompletableFuture.completedFuture("STEP_OK:" + id);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
