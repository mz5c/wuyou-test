package com.wuyou.onlytest.ratelimit.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedRateLimit {
    String key() default "default";
    int maxRequests() default 10;
    int windowMs() default 1000;
}
