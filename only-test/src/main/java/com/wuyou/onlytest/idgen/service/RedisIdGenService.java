package com.wuyou.onlytest.idgen.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisIdGenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "idgen:";

    public Long nextId(String bizType) {
        return redisTemplate.opsForValue().increment(KEY_PREFIX + bizType);
    }

    public List<Long> batchNextId(String bizType, int count) {
        List<Long> ids = new ArrayList<>(count);
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) {
            ids.add(nextId(bizType));
        }
        long cost = (System.nanoTime() - start) / 1000;
        log.info("generated {} redis ids for {} in {} us", count, bizType, cost);
        return ids;
    }
}
