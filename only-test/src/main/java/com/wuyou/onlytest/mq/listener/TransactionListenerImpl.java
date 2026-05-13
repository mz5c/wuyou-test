package com.wuyou.onlytest.mq.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;

@Slf4j
@Profile("full")
@RocketMQTransactionListener
public class TransactionListenerImpl implements RocketMQLocalTransactionListener {

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        log.info("executing local transaction for msg: {}", msg.getPayload());
        try {
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            log.error("local transaction failed", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        log.info("checking local transaction for msg: {}", msg.getPayload());
        return RocketMQLocalTransactionState.COMMIT;
    }
}
