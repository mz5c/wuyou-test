package com.wuyou.onlytest.idempotent.service;

import com.wuyou.common.exception.BizException;
import com.wuyou.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotentDbService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    public String payOrder(String bizType, String bizId) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO demo_idempotent_record(biz_type, biz_id, status) VALUES(?, ?, 0)",
                    bizType, bizId);
        } catch (DuplicateKeyException e) {
            String status = jdbcTemplate.queryForObject(
                    "SELECT status FROM demo_idempotent_record WHERE biz_type=? AND biz_id=?",
                    String.class, bizType, bizId);
            if ("1".equals(status)) {
                return "DUPLICATE_DONE:" + bizId;
            }
            throw new BizException(ResultCode.BIZ_ERROR, "请求正在处理中");
        }

        log.info("processing payment: bizType={}, bizId={}", bizType, bizId);
        jdbcTemplate.update(
                "UPDATE demo_idempotent_record SET status=1 WHERE biz_type=? AND biz_id=?",
                bizType, bizId);
        return "PAY_SUCCESS:" + bizId;
    }
}
