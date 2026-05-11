package com.wuyou.onlytest.seata.service;

import io.seata.saga.engine.StateMachineEngine;
import io.seata.saga.statelang.domain.ExecutionStatus;
import io.seata.saga.statelang.domain.StateMachineInstance;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeataSagaService {

    private final StateMachineEngine stateMachineEngine;

    @GlobalTransactional
    public String createOrderBySaga(Long userId, Long productId, int quantity) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("productId", productId);
        params.put("quantity", quantity);

        StateMachineInstance instance = stateMachineEngine.start(
                "order-fulfillment-saga", null, params);
        log.info("saga instance: id={}, status={}", instance.getId(), instance.getStatus());

        if (ExecutionStatus.SU.equals(instance.getStatus())) {
            return "SAGA_SUCCESS:" + instance.getId();
        }
        return "SAGA_FAILED:" + instance.getStatus();
    }
}
