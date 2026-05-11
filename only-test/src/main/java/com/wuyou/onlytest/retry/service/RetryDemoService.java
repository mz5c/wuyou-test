package com.wuyou.onlytest.retry.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class RetryDemoService {

    private final AtomicInteger attemptCounter = new AtomicInteger(0);

    @Retryable(
            value = RuntimeException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public String unstableCall(boolean shouldFail) {
        int attempt = attemptCounter.incrementAndGet();
        log.info("unstableCall attempt {}", attempt);
        if (shouldFail && attempt < 3) {
            throw new RuntimeException("transient failure, attempt=" + attempt);
        }
        return "SUCCESS after " + attempt + " attempts";
    }

    @Recover
    public String recover(RuntimeException e) {
        log.error("all retries exhausted", e);
        return "FALLBACK: " + e.getMessage();
    }

    public void resetCounter() {
        attemptCounter.set(0);
    }
}
