package com.wuyou.onlytest.ratelimit.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LocalRateLimit {
    double permitsPerSecond() default 10.0;
    int timeoutMs() default 0;
}
