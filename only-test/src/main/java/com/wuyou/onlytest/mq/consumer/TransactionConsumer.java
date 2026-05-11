package com.wuyou.onlytest.mq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "mq.consumer.enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = "demo-order-transaction",
        consumerGroup = "demo-transaction-consumer"
)
public class TransactionConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        log.info("received transaction msg: {}", message);
    }
}
