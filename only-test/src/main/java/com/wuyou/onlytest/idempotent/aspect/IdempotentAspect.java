package com.wuyou.onlytest.idempotent.aspect;

import com.wuyou.common.exception.BizException;
import com.wuyou.common.result.ResultCode;
import com.wuyou.onlytest.idempotent.annotation.Idempotent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SpelExpressionParser spelParser;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String bizId = spelParser.parse(idempotent.key(), joinPoint);
        String key = "idempotent:" + idempotent.bizType() + ":" + bizId;

        Boolean absent = redisTemplate.opsForValue()
                .setIfAbsent(key, "PROCESSING", Duration.ofSeconds(idempotent.ttlSeconds()));
        if (Boolean.FALSE.equals(absent)) {
            Object status = redisTemplate.opsForValue().get(key);
            if ("DONE".equals(status)) {
                Object result = redisTemplate.opsForValue().get(key + ":result");
                if (result != null) {
                    log.info("idempotent hit, returning cached result for {}", key);
                    return result;
                }
                throw new BizException(ResultCode.BIZ_ERROR, "请求结果已过期，请重新提交");
            }
            throw new BizException(ResultCode.BIZ_ERROR, "请求正在处理中，请勿重复提交");
        }

        try {
            Object result = joinPoint.proceed();
            redisTemplate.opsForValue().set(key, "DONE", Duration.ofSeconds(idempotent.ttlSeconds()));
            if (result != null) {
                redisTemplate.opsForValue().set(key + ":result", result, Duration.ofSeconds(idempotent.ttlSeconds()));
            }
            return result;
        } catch (Throwable e) {
            redisTemplate.delete(key);
            throw e;
        }
    }
}
