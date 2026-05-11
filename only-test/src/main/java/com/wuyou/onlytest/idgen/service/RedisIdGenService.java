package com.wuyou.onlytest.idgen.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisIdGenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "idgen:";

    private DefaultRedisScript<Long> incrScript;
    @SuppressWarnings("rawtypes")
    private DefaultRedisScript incrByScript;

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        incrScript = new DefaultRedisScript<>();
        incrScript.setScriptText(
                "local val = redis.call('INCR', KEYS[1])\n" +
                "redis.call('EXPIRE', KEYS[1], 86400)\n" +
                "return val");
        incrScript.setResultType(Long.class);

        incrByScript = new DefaultRedisScript();
        incrByScript.setScriptText(
                "local newVal = redis.call('INCRBY', KEYS[1], ARGV[1])\n" +
                "redis.call('EXPIRE', KEYS[1], 86400)\n" +
                "local startVal = newVal - tonumber(ARGV[1]) + 1\n" +
                "return {startVal, newVal}");
        incrByScript.setResultType((Class) List.class);
    }

    public Long nextId(String bizType) {
        return redisTemplate.execute(incrScript, Collections.singletonList(KEY_PREFIX + bizType));
    }

    @SuppressWarnings("unchecked")
    public List<Long> batchNextId(String bizType, int count) {
        List<Long> range = (List<Long>) redisTemplate.execute(incrByScript,
                Collections.singletonList(KEY_PREFIX + bizType), count);
        long start = ((Number) range.get(0)).longValue();
        long end = ((Number) range.get(1)).longValue();
        List<Long> ids = new ArrayList<>(count);
        long begin = System.nanoTime();
        for (long i = start; i <= end; i++) {
            ids.add(i);
        }
        long cost = (System.nanoTime() - begin) / 1000;
        log.info("generated {} redis ids for {} in {} us", count, bizType, cost);
        return ids;
    }
}
