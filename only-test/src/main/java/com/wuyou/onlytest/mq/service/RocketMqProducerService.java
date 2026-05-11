package com.wuyou.onlytest.mq.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RocketMqProducerService {

    private final RocketMQTemplate rocketMQTemplate;

    public SendResult sendSync(String topic, String body) {
        SendResult result = rocketMQTemplate.syncSend(topic, body);
        log.info("sync send result: {}", result);
        return result;
    }

    public void sendAsync(String topic, String body) {
        rocketMQTemplate.asyncSend(topic, body, new SendCallback() {
            @Override
            public void onSuccess(SendResult result) {
                log.info("async send success: {}", result);
            }
            @Override
            public void onException(Throwable e) {
                log.error("async send failed", e);
            }
        });
    }

    public void sendOneWay(String topic, String body) {
        rocketMQTemplate.sendOneWay(topic, body);
        log.info("oneway sent: {}", body);
    }

    public SendResult sendOrderly(String topic, String body, String orderKey) {
        SendResult result = rocketMQTemplate.syncSendOrderly(topic, body, orderKey);
        log.info("orderly send result: {}", result);
        return result;
    }

    public void sendTransaction(String topic, String body) {
        Message<String> msg = MessageBuilder.withPayload(body)
                .setHeader("keys", UUID.randomUUID().toString())
                .build();
        SendResult result = rocketMQTemplate.sendMessageInTransaction(topic, msg, null);
        log.info("transaction msg result: {}", result);
    }
}
