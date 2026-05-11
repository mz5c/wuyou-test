package com.wuyou.onlytest.seata.service;

import com.wuyou.common.exception.BizException;
import com.wuyou.common.result.ResultCode;
import com.wuyou.onlytest.seata.entity.Account;
import com.wuyou.onlytest.seata.mapper.AccountMapper;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@LocalTCC
@Service
@RequiredArgsConstructor
public class SeataTccService {

    private final AccountMapper accountMapper;

    @TwoPhaseBusinessAction(name = "transferTcc", commitMethod = "commit", rollbackMethod = "rollback")
    @Transactional(rollbackFor = Exception.class)
    public void tryTransfer(@BusinessActionContextParameter(paramName = "fromUserId") Long fromUserId,
                            @BusinessActionContextParameter(paramName = "toUserId") Long toUserId,
                            @BusinessActionContextParameter(paramName = "amount") BigDecimal amount) {
        log.info("TCC try: freeze {} from user {} to user {}", amount, fromUserId, toUserId);
        Account from = accountMapper.selectByUserId(fromUserId);
        if (from == null || from.getBalance().compareTo(amount) < 0) {
            throw new BizException(ResultCode.BIZ_ERROR, "insufficient balance");
        }
        from.setBalance(from.getBalance().subtract(amount));
        from.setFrozen(from.getFrozen().add(amount));
        accountMapper.updateById(from);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean commit(BusinessActionContext ctx) {
        Long fromUserId = Long.valueOf(ctx.getActionContext("fromUserId").toString());
        Long toUserId = Long.valueOf(ctx.getActionContext("toUserId").toString());
        BigDecimal amount = new BigDecimal(ctx.getActionContext("amount").toString());
        log.info("TCC commit: deduct frozen from {} and add to {}", fromUserId, toUserId);

        Account from = accountMapper.selectByUserId(fromUserId);
        from.setFrozen(from.getFrozen().subtract(amount));
        accountMapper.updateById(from);

        Account to = accountMapper.selectByUserId(toUserId);
        to.setBalance(to.getBalance().add(amount));
        accountMapper.updateById(to);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean rollback(BusinessActionContext ctx) {
        Long fromUserId = Long.valueOf(ctx.getActionContext("fromUserId").toString());
        BigDecimal amount = new BigDecimal(ctx.getActionContext("amount").toString());
        log.info("TCC rollback: unfreeze {} from user {}", amount, fromUserId);

        Account from = accountMapper.selectByUserId(fromUserId);
        from.setBalance(from.getBalance().add(amount));
        from.setFrozen(from.getFrozen().subtract(amount));
        accountMapper.updateById(from);
        return true;
    }
}
