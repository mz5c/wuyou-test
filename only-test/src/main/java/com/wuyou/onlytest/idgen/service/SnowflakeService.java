package com.wuyou.onlytest.idgen.service;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SnowflakeService {

    private Snowflake snowflake;

    @PostConstruct
    public void init() {
        snowflake = IdUtil.getSnowflake(1, 1);
    }

    public long nextId() {
        return snowflake.nextId();
    }

    public List<Long> batchNextId(int count) {
        List<Long> ids = new ArrayList<>(count);
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) {
            ids.add(snowflake.nextId());
        }
        long cost = (System.nanoTime() - start) / 1000;
        log.info("generated {} snowflake ids in {} us", count, cost);
        return ids;
    }
}
