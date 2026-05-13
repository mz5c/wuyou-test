package com.wuyou.onlytest.seata.config;

import io.seata.saga.engine.StateMachineEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("full")
@RequiredArgsConstructor
public class SagaStateMachineInit {

    private final StateMachineEngine stateMachineEngine;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            Resource[] resources = new Resource[]{
                    new ClassPathResource("saga/order-fulfillment-saga.json")
            };
            log.info("Registering saga state machine from classpath:saga/order-fulfillment-saga.json");
            stateMachineEngine.getStateMachineConfig()
                    .getStateMachineRepository()
                    .registryByResources(resources, "");
            log.info("Saga state machine registered successfully");
        } catch (Exception e) {
            log.warn("Failed to register saga state machine: {}", e.getMessage());
        }
    }
}
