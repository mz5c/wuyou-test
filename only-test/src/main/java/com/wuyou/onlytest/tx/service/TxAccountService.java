package com.wuyou.onlytest.tx.service;

import cn.hutool.core.util.IdUtil;
import com.wuyou.common.exception.BizException;
import com.wuyou.common.result.ResultCode;
import com.wuyou.onlytest.tx.dto.TxAccountVO;
import com.wuyou.onlytest.tx.entity.TxAccount;
import com.wuyou.onlytest.tx.mapper.TxAccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TxAccountService {

    private final TxAccountMapper accountMapper;

    @Transactional(rollbackFor = Exception.class)
    public TxAccountVO openAccount(Long userId) {
        TxAccount exist = accountMapper.selectByUserId(userId);
        if (exist != null) {
            throw new BizException(ResultCode.BIZ_ERROR, "账户已存在: userId=" + userId);
        }

        TxAccount account = new TxAccount();
        account.setUserId(userId);
        account.setAccountNo("ACC" + IdUtil.getSnowflakeNextIdStr());
        account.setBalance(BigDecimal.ZERO);
        account.setFrozen(BigDecimal.ZERO);
        account.setStatus(1);

        accountMapper.insert(account);
        log.info("account opened: userId={}, accountNo={}", userId, account.getAccountNo());
        return TxAccountVO.from(account);
    }

    @Transactional(rollbackFor = Exception.class)
    public TxAccountVO recharge(Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ResultCode.PARAM_ERROR, "充值金额必须为正数");
        }

        TxAccount account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new BizException(ResultCode.NOT_FOUND, "账户不存在: id=" + accountId);
        }

        account.setBalance(account.getBalance().add(amount));
        if (accountMapper.updateById(account) == 0) {
            throw new BizException(ResultCode.DB_ERROR, "充值失败，数据冲突请重试");
        }
        log.info("account recharged: id={}, amount={}", accountId, amount);
        return TxAccountVO.from(account);
    }

    public TxAccountVO getById(Long id) {
        TxAccount account = accountMapper.selectById(id);
        if (account == null) {
            throw new BizException(ResultCode.NOT_FOUND, "账户不存在: id=" + id);
        }
        return TxAccountVO.from(account);
    }
}
