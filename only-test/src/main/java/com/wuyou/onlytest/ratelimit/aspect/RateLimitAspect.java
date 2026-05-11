package com.wuyou.onlytest.ratelimit.aspect;

import com.google.common.util.concurrent.RateLimiter;
import com.wuyou.common.exception.BizException;
import com.wuyou.common.result.ResultCode;
import com.wuyou.onlytest.ratelimit.annotation.DistributedRateLimit;
import com.wuyou.onlytest.ratelimit.annotation.LocalRateLimit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private DefaultRedisScript<Long> luaScript;
    private final Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    public RateLimitAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        luaScript = new DefaultRedisScript<>();
        luaScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/rate_limiter.lua")));
        luaScript.setResultType(Long.class);
    }

    @Around("@annotation(limit)")
    public Object aroundLocal(ProceedingJoinPoint joinPoint, LocalRateLimit limit) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        RateLimiter limiter = rateLimiterMap.computeIfAbsent(methodName,
                k -> RateLimiter.create(limit.permitsPerSecond()));
        if (!limiter.tryAcquire(limit.timeoutMs(), java.util.concurrent.TimeUnit.MILLISECONDS)) {
            throw new BizException(ResultCode.BIZ_ERROR, "本地限流触发，请求被拒绝");
        }
        return joinPoint.proceed();
    }

    @Around("@annotation(limit)")
    public Object aroundDistributed(ProceedingJoinPoint joinPoint, DistributedRateLimit limit) throws Throwable {
        String key = "ratelimit:distributed:" + limit.key();
        List<String> keys = Collections.singletonList(key);
        Long allowed = redisTemplate.execute(luaScript, keys,
                String.valueOf(limit.windowMs()), String.valueOf(limit.maxRequests()));
        if (Long.valueOf(0).equals(allowed)) {
            throw new BizException(ResultCode.BIZ_ERROR, "分布式限流触发，请求被拒绝");
        }
        return joinPoint.proceed();
    }
}
