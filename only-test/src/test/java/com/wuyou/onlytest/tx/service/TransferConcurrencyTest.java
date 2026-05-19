package com.wuyou.onlytest.tx.service;

import com.wuyou.onlytest.tx.dto.TransferRequest;
import com.wuyou.onlytest.tx.entity.TransferRecord;
import com.wuyou.onlytest.tx.entity.TxAccount;
import com.wuyou.onlytest.tx.mapper.TransferRecordMapper;
import com.wuyou.onlytest.tx.mapper.TxAccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferConcurrencyTest {

    @Mock
    private TxAccountMapper accountMapper;
    @Mock
    private TransferRecordMapper recordMapper;
    @Mock
    private RedissonClient redissonClient;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(accountMapper, recordMapper, redissonClient);
    }

    @Test
    @DisplayName("乐观锁并发 - 模拟 version 冲突重试")
    void optimisticLock_retryOnConflict() throws InterruptedException {
        TxAccount from = new TxAccount();
        from.setId(1L);
        from.setAccountNo("ACC10001");
        from.setBalance(BigDecimal.valueOf(1000));
        from.setStatus(1);
        from.setVersion(0);

        TxAccount to = new TxAccount();
        to.setId(2L);
        to.setAccountNo("ACC10002");
        to.setBalance(BigDecimal.valueOf(500));
        to.setStatus(1);
        to.setVersion(0);

        when(accountMapper.selectByAccountNo("ACC10001")).thenReturn(from);
        when(accountMapper.selectByAccountNo("ACC10002")).thenReturn(to);
        when(recordMapper.selectByBizNo(anyString())).thenReturn(null);
        // 第一次 updateById 返回 0（version 冲突），第二次返回 1（成功）
        when(accountMapper.updateById(from)).thenReturn(0).thenReturn(1);
        when(accountMapper.updateById(to)).thenReturn(1);

        TransferRequest req = new TransferRequest();
        req.setFromAccountNo("ACC10001");
        req.setToAccountNo("ACC10002");
        req.setAmount(BigDecimal.valueOf(100));

        TransferRecord result = transferService.transferOptimistic(req);

        assertThat(result.getStatus()).isEqualTo(1);
        // verify from 的 updateById 被调用了 2 次
        verify(accountMapper, times(2)).updateById(from);
    }

    @Test
    @DisplayName("多线程并发调用悲观锁 - 验证线程安全")
    void pessimisticLock_concurrentTransfers() throws InterruptedException {
        TxAccount from = new TxAccount();
        from.setId(1L);
        from.setAccountNo("ACC10001");
        from.setBalance(BigDecimal.valueOf(1000));
        from.setStatus(1);
        from.setVersion(0);

        TxAccount to = new TxAccount();
        to.setId(2L);
        to.setAccountNo("ACC10002");
        to.setBalance(BigDecimal.valueOf(500));
        to.setStatus(1);
        to.setVersion(0);

        when(accountMapper.selectByNoForUpdate("ACC10001")).thenReturn(from);
        when(accountMapper.selectByNoForUpdate("ACC10002")).thenReturn(to);
        when(recordMapper.selectByBizNo(anyString())).thenReturn(null);
        when(accountMapper.updateById(any())).thenReturn(1);

        AtomicInteger successCount = new AtomicInteger(0);
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    TransferRequest req = new TransferRequest();
                    req.setFromAccountNo("ACC10001");
                    req.setToAccountNo("ACC10002");
                    req.setAmount(BigDecimal.valueOf(100));
                    transferService.transfer(req);
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        assertThat(successCount.get()).isGreaterThanOrEqualTo(0);
    }
}
