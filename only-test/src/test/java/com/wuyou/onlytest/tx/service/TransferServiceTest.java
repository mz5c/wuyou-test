package com.wuyou.onlytest.tx.service;

import com.wuyou.common.exception.BizException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TxAccountMapper accountMapper;
    @Mock
    private TransferRecordMapper recordMapper;
    @Mock
    private RedissonClient redissonClient;

    private TransferService transferService;

    private TxAccount fromAccount;
    private TxAccount toAccount;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(accountMapper, recordMapper, redissonClient);

        fromAccount = new TxAccount();
        fromAccount.setId(1L);
        fromAccount.setAccountNo("ACC10001");
        fromAccount.setBalance(BigDecimal.valueOf(1000));
        fromAccount.setStatus(1);
        fromAccount.setVersion(0);

        toAccount = new TxAccount();
        toAccount.setId(2L);
        toAccount.setAccountNo("ACC10002");
        toAccount.setBalance(BigDecimal.valueOf(500));
        toAccount.setStatus(1);
        toAccount.setVersion(0);
    }

    @Test
    @DisplayName("正常转账 - 双方余额正确变更")
    void transfer_success() {
        when(accountMapper.selectByNoForUpdate("ACC10001")).thenReturn(fromAccount);
        when(accountMapper.selectByNoForUpdate("ACC10002")).thenReturn(toAccount);
        when(recordMapper.selectByBizNo(anyString())).thenReturn(null);
        when(recordMapper.insert(any(TransferRecord.class))).thenReturn(1);

        TransferRequest req = new TransferRequest();
        req.setFromAccountNo("ACC10001");
        req.setToAccountNo("ACC10002");
        req.setAmount(BigDecimal.valueOf(200));

        TransferRecord result = transferService.transfer(req);

        assertThat(result.getStatus()).isEqualTo(1);
        assertThat(fromAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(800));
        assertThat(toAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700));
        verify(accountMapper).updateById(fromAccount);
        verify(accountMapper).updateById(toAccount);
    }

    @Test
    @DisplayName("同账户转账 - 抛出异常")
    void transfer_sameAccount_throws() {
        TransferRequest req = new TransferRequest();
        req.setFromAccountNo("ACC10001");
        req.setToAccountNo("ACC10001");
        req.setAmount(BigDecimal.valueOf(100));

        assertThatThrownBy(() -> transferService.transfer(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("相同");
    }

    @Test
    @DisplayName("余额不足 - 抛出异常")
    void transfer_insufficientBalance_throws() {
        when(accountMapper.selectByNoForUpdate("ACC10001")).thenReturn(fromAccount);
        when(recordMapper.selectByBizNo(anyString())).thenReturn(null);

        TransferRequest req = new TransferRequest();
        req.setFromAccountNo("ACC10001");
        req.setToAccountNo("ACC10002");
        req.setAmount(BigDecimal.valueOf(9999));

        assertThatThrownBy(() -> transferService.transfer(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不足");
    }

    @Test
    @DisplayName("幂等 - 相同 bizNo 重复请求直接返回")
    void transfer_idempotent_returnsExisting() {
        TransferRecord existing = new TransferRecord();
        existing.setBizNo("BIZ001");
        existing.setStatus(1);
        when(recordMapper.selectByBizNo("BIZ001")).thenReturn(existing);

        TransferRequest req = new TransferRequest();
        req.setFromAccountNo("ACC10001");
        req.setToAccountNo("ACC10002");
        req.setAmount(BigDecimal.valueOf(100));
        req.setBizNo("BIZ001");

        TransferRecord result = transferService.transfer(req);

        assertThat(result.getStatus()).isEqualTo(1);
        verify(accountMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("转入账户不存在 - 抛出异常")
    void transfer_toAccountNotFound_throws() {
        when(accountMapper.selectByNoForUpdate("ACC10001")).thenReturn(fromAccount);
        when(accountMapper.selectByNoForUpdate("ACC10002")).thenReturn(null);
        when(recordMapper.selectByBizNo(anyString())).thenReturn(null);

        TransferRequest req = new TransferRequest();
        req.setFromAccountNo("ACC10001");
        req.setToAccountNo("ACC10002");
        req.setAmount(BigDecimal.valueOf(100));

        assertThatThrownBy(() -> transferService.transfer(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("乐观锁转账成功 - from 端 version 冲突后重试")
    void transferOptimistic_retrySuccess() {
        when(accountMapper.selectByAccountNo("ACC10001")).thenReturn(fromAccount);
        when(accountMapper.selectByAccountNo("ACC10002")).thenReturn(toAccount);
        when(recordMapper.selectByBizNo(anyString())).thenReturn(null);
        when(accountMapper.updateById(fromAccount)).thenReturn(0).thenReturn(1);
        when(accountMapper.updateById(toAccount)).thenReturn(1);

        TransferRequest req = new TransferRequest();
        req.setFromAccountNo("ACC10001");
        req.setToAccountNo("ACC10002");
        req.setAmount(BigDecimal.valueOf(100));

        TransferRecord result = transferService.transferOptimistic(req);

        assertThat(result.getStatus()).isEqualTo(1);
        verify(accountMapper, times(2)).updateById(fromAccount);
        verify(accountMapper, times(1)).updateById(toAccount);
    }

    @Test
    @DisplayName("乐观锁转账 - 转入方冲突抛出异常")
    void transferOptimistic_toConflict_throws() {
        when(accountMapper.selectByAccountNo("ACC10001")).thenReturn(fromAccount);
        when(accountMapper.selectByAccountNo("ACC10002")).thenReturn(toAccount);
        when(recordMapper.selectByBizNo(anyString())).thenReturn(null);
        when(accountMapper.updateById(fromAccount)).thenReturn(1);
        when(accountMapper.updateById(toAccount)).thenReturn(0);

        TransferRequest req = new TransferRequest();
        req.setFromAccountNo("ACC10001");
        req.setToAccountNo("ACC10002");
        req.setAmount(BigDecimal.valueOf(100));

        assertThatThrownBy(() -> transferService.transferOptimistic(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("转入方");
    }

    @Test
    @DisplayName("乐观锁转账 - 重试耗尽抛出异常")
    void transferOptimistic_retryExhausted_throws() {
        when(accountMapper.selectByAccountNo("ACC10001")).thenReturn(fromAccount);
        when(accountMapper.selectByAccountNo("ACC10002")).thenReturn(toAccount);
        when(recordMapper.selectByBizNo(anyString())).thenReturn(null);
        when(accountMapper.updateById(any())).thenReturn(0);

        TransferRequest req = new TransferRequest();
        req.setFromAccountNo("ACC10001");
        req.setToAccountNo("ACC10002");
        req.setAmount(BigDecimal.valueOf(100));

        assertThatThrownBy(() -> transferService.transferOptimistic(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("重试耗尽");
    }

    @Test
    @DisplayName("转出账户冻结 - 抛出异常")
    void transfer_fromAccountFrozen_throws() {
        fromAccount.setStatus(2);  // 冻结
        when(accountMapper.selectByNoForUpdate("ACC10001")).thenReturn(fromAccount);
        when(recordMapper.selectByBizNo(anyString())).thenReturn(null);

        TransferRequest req = new TransferRequest();
        req.setFromAccountNo("ACC10001");
        req.setToAccountNo("ACC10002");
        req.setAmount(BigDecimal.valueOf(100));

        assertThatThrownBy(() -> transferService.transfer(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("状态异常");
    }

    @Test
    @DisplayName("分布式锁转账成功")
    void transferWithLock_success() {
        RLock mockLock = mock(RLock.class);
        when(redissonClient.getLock("tx:lock:ACC10001")).thenReturn(mockLock);
        when(accountMapper.selectByAccountNo("ACC10001")).thenReturn(fromAccount);
        when(accountMapper.selectByAccountNo("ACC10002")).thenReturn(toAccount);
        when(recordMapper.selectByBizNo(anyString())).thenReturn(null);
        when(accountMapper.updateById(any())).thenReturn(1);

        TransferRequest req = new TransferRequest();
        req.setFromAccountNo("ACC10001");
        req.setToAccountNo("ACC10002");
        req.setAmount(BigDecimal.valueOf(100));

        TransferRecord result = transferService.transferWithLock(req);

        assertThat(result.getStatus()).isEqualTo(1);
        verify(mockLock).lock();
        verify(mockLock).unlock();
    }
}
