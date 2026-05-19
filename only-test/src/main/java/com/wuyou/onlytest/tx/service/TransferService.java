package com.wuyou.onlytest.tx.service;

import cn.hutool.core.util.IdUtil;
import com.wuyou.common.exception.BizException;
import com.wuyou.common.result.ResultCode;
import com.wuyou.onlytest.tx.dto.TransferRequest;
import com.wuyou.onlytest.tx.entity.TransferRecord;
import com.wuyou.onlytest.tx.entity.TxAccount;
import com.wuyou.onlytest.tx.mapper.TransferRecordMapper;
import com.wuyou.onlytest.tx.mapper.TxAccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TxAccountMapper accountMapper;
    private final TransferRecordMapper recordMapper;
    private final RedissonClient redissonClient;

    /**
     * 基础转账 — REQUIRED 事务，SELECT FOR UPDATE 行锁
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferRecord transfer(TransferRequest req) {
        if (req.getFromAccountNo().equals(req.getToAccountNo())) {
            throw new BizException(ResultCode.BIZ_ERROR, "转出和转入账户不能相同");
        }

        String bizNo = req.getBizNo() != null ? req.getBizNo() : "BIZ" + IdUtil.getSnowflakeNextIdStr();
        TransferRecord exist = recordMapper.selectByBizNo(bizNo);
        if (exist != null) {
            log.info("idempotent hit: bizNo={}, status={}", bizNo, exist.getStatus());
            return exist;
        }

        TxAccount from = accountMapper.selectByNoForUpdate(req.getFromAccountNo());
        validateAccount(from, req.getFromAccountNo());
        if (from.getBalance().compareTo(req.getAmount()) < 0) {
            throw new BizException(ResultCode.BIZ_ERROR, "余额不足: account=" + req.getFromAccountNo());
        }
        from.setBalance(from.getBalance().subtract(req.getAmount()));
        accountMapper.updateById(from);

        TxAccount to = accountMapper.selectByNoForUpdate(req.getToAccountNo());
        validateAccount(to, req.getToAccountNo());
        to.setBalance(to.getBalance().add(req.getAmount()));
        accountMapper.updateById(to);

        TransferRecord record = new TransferRecord();
        record.setBizNo(bizNo);
        record.setFromAccountNo(req.getFromAccountNo());
        record.setToAccountNo(req.getToAccountNo());
        record.setAmount(req.getAmount());
        record.setStatus(1);
        recordMapper.insert(record);

        log.info("transfer success: {} -> {}, amount={}, bizNo={}",
                req.getFromAccountNo(), req.getToAccountNo(), req.getAmount(), bizNo);
        return record;
    }

    /**
     * REQUIRES_NEW 演示：扣款在独立事务中，外围异常不影响扣款结果
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void deductRequiresNew(String accountNo, BigDecimal amount) {
        TxAccount account = accountMapper.selectByNoForUpdate(accountNo);
        validateAccount(account, accountNo);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BizException(ResultCode.BIZ_ERROR, "余额不足: " + accountNo);
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountMapper.updateById(account);
        log.info("deductRequiresNew: {} amount={}", accountNo, amount);
    }

    /**
     * NESTED 演示：扣款基于 Savepoint，外围可选择性回滚
     */
    @Transactional(propagation = Propagation.NESTED, rollbackFor = Exception.class)
    public void deductNested(String accountNo, BigDecimal amount) {
        TxAccount account = accountMapper.selectByNoForUpdate(accountNo);
        validateAccount(account, accountNo);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BizException(ResultCode.BIZ_ERROR, "余额不足: " + accountNo);
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountMapper.updateById(account);
        log.info("deductNested: {} amount={}", accountNo, amount);
    }

    /**
     * 悲观锁转账 — 显式 SELECT FOR UPDATE
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferRecord transferPessimistic(TransferRequest req) {
        return transfer(req);
    }

    /**
     * 乐观锁转账 — 基于 @Version 重试
     * 注意：仅在转出方 updateById 冲突时重试；转入方冲突时直接抛出异常让事务回滚，
     *       避免转出方已扣款但转入方失败导致的双重扣款问题。
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferRecord transferOptimistic(TransferRequest req) {
        if (req.getFromAccountNo().equals(req.getToAccountNo())) {
            throw new BizException(ResultCode.BIZ_ERROR, "转出和转入账户不能相同");
        }

        String bizNo = req.getBizNo() != null ? req.getBizNo() : "BIZ" + IdUtil.getSnowflakeNextIdStr();
        TransferRecord exist = recordMapper.selectByBizNo(bizNo);
        if (exist != null) {
            return exist;
        }

        int retries = 0;
        int maxRetries = 3;
        while (retries < maxRetries) {
            TxAccount from = accountMapper.selectByAccountNo(req.getFromAccountNo());
            validateAccount(from, req.getFromAccountNo());
            if (from.getBalance().compareTo(req.getAmount()) < 0) {
                throw new BizException(ResultCode.BIZ_ERROR, "余额不足");
            }
            from.setBalance(from.getBalance().subtract(req.getAmount()));

            TxAccount to = accountMapper.selectByAccountNo(req.getToAccountNo());
            validateAccount(to, req.getToAccountNo());
            to.setBalance(to.getBalance().add(req.getAmount()));

            int fromRows = accountMapper.updateById(from);
            if (fromRows == 0) {
                retries++;
                log.warn("optimistic lock retry {}/{} for bizNo={}", retries, maxRetries, bizNo);
                continue;
            }

            // 转入方冲突时不重试，抛出异常让事务回滚，避免转出方已扣款导致不一致
            int toRows = accountMapper.updateById(to);
            if (toRows == 0) {
                throw new BizException(ResultCode.BIZ_ERROR,
                        "乐观锁冲突，转入方更新失败，事务回滚");
            }

            TransferRecord record = new TransferRecord();
            record.setBizNo(bizNo);
            record.setFromAccountNo(req.getFromAccountNo());
            record.setToAccountNo(req.getToAccountNo());
            record.setAmount(req.getAmount());
            record.setStatus(1);
            recordMapper.insert(record);
            return record;
        }
        throw new BizException(ResultCode.BIZ_ERROR, "乐观锁转账失败，重试耗尽");
    }

    /**
     * 分布式锁转账 — Redisson RLock
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferRecord transferWithLock(TransferRequest req) {
        if (req.getFromAccountNo().equals(req.getToAccountNo())) {
            throw new BizException(ResultCode.BIZ_ERROR, "转出和转入账户不能相同");
        }

        String bizNo = req.getBizNo() != null ? req.getBizNo() : "BIZ" + IdUtil.getSnowflakeNextIdStr();
        TransferRecord exist = recordMapper.selectByBizNo(bizNo);
        if (exist != null) {
            return exist;
        }

        RLock lock = redissonClient.getLock("tx:lock:" + req.getFromAccountNo());
        lock.lock();
        try {
            TxAccount from = accountMapper.selectByAccountNo(req.getFromAccountNo());
            validateAccount(from, req.getFromAccountNo());
            if (from.getBalance().compareTo(req.getAmount()) < 0) {
                throw new BizException(ResultCode.BIZ_ERROR, "余额不足");
            }
            from.setBalance(from.getBalance().subtract(req.getAmount()));
            accountMapper.updateById(from);

            TxAccount to = accountMapper.selectByAccountNo(req.getToAccountNo());
            validateAccount(to, req.getToAccountNo());
            to.setBalance(to.getBalance().add(req.getAmount()));
            accountMapper.updateById(to);

            TransferRecord record = new TransferRecord();
            record.setBizNo(bizNo);
            record.setFromAccountNo(req.getFromAccountNo());
            record.setToAccountNo(req.getToAccountNo());
            record.setAmount(req.getAmount());
            record.setStatus(1);
            recordMapper.insert(record);
            return record;
        } finally {
            lock.unlock();
        }
    }

    private void validateAccount(TxAccount account, String accountNo) {
        if (account == null) {
            throw new BizException(ResultCode.NOT_FOUND, "账户不存在: " + accountNo);
        }
        if (!Integer.valueOf(1).equals(account.getStatus())) {
            throw new BizException(ResultCode.BIZ_ERROR, "账户状态异常: " + accountNo);
        }
    }
}
