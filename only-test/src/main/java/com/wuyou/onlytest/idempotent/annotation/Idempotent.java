package com.wuyou.onlytest.idempotent.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    String key();
    String bizType();
    int ttlSeconds() default 86400;
}
