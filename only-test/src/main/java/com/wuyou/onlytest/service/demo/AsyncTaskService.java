package com.wuyou.onlytest.service.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AsyncTaskService {

    @Async
    public void sendNotification(Long userId, String message) {
        log.info("async send notification to user {}: {}", userId, message);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("notification sent to user {}", userId);
    }

    @Async
    public void logOperation(String operation, String detail) {
        log.info("async op log: {} - {}", operation, detail);
    }
}
