package com.wuyou.onlytest.idempotent.service;

import com.wuyou.onlytest.idempotent.annotation.Idempotent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IdempotentRedisService {

    @Idempotent(key = "#orderId", bizType = "order:pay", ttlSeconds = 3600)
    public String payOrder(Long orderId, Long userId) {
        log.info("processing payment: orderId={}, userId={}", orderId, userId);
        return "PAY_SUCCESS:" + orderId;
    }
}
