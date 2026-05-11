package com.wuyou.onlytest.ratelimit.service;

import com.wuyou.onlytest.ratelimit.annotation.DistributedRateLimit;
import com.wuyou.onlytest.ratelimit.annotation.LocalRateLimit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RateLimitService {

    @LocalRateLimit(permitsPerSecond = 5.0)
    public String localLimited() {
        log.info("local limited api called");
        return "LOCAL_OK";
    }

    @DistributedRateLimit(key = "stress", maxRequests = 3, windowMs = 1000)
    public String distributedLimited() {
        log.info("distributed limited api called");
        return "DIST_OK";
    }
}
