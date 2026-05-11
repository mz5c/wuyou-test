package com.wuyou.onlytest.mq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "mq.consumer.enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = "demo-order",
        consumerGroup = "demo-order-consumer",
        consumeMode = ConsumeMode.ORDERLY,
        messageModel = MessageModel.CLUSTERING
)
public class OrderStatusConsumer implements RocketMQListener<MessageExt> {

    @Override
    public void onMessage(MessageExt msg) {
        log.info("received order msg: queueId={}, body={}, keys={}",
                msg.getQueueId(), new String(msg.getBody()), msg.getKeys());
    }
}
