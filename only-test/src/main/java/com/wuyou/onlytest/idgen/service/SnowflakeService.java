package com.wuyou.onlytest.idgen.service;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SnowflakeService {

    private Snowflake snowflake;

    @Value("${idgen.snowflake.worker-id:1}")
    private long workerId;

    @Value("${idgen.snowflake.datacenter-id:1}")
    private long datacenterId;

    @PostConstruct
    public void init() {
        snowflake = IdUtil.getSnowflake(workerId, datacenterId);
        log.info("Snowflake initialized with workerId={}, datacenterId={}", workerId, datacenterId);
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
