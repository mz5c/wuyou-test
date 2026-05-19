package com.wuyou.onlytest.tx.service;

import com.wuyou.common.exception.BizException;
import com.wuyou.onlytest.tx.dto.TxAccountVO;
import com.wuyou.onlytest.tx.entity.TxAccount;
import com.wuyou.onlytest.tx.mapper.TxAccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TxAccountServiceTest {

    @Mock
    private TxAccountMapper accountMapper;

    private TxAccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new TxAccountService(accountMapper);
    }

    @Test
    @DisplayName("开户成功 - 生成 account_no 并写入数据库")
    void openAccount_success() {
        when(accountMapper.insert(any(TxAccount.class))).thenReturn(1);

        TxAccountVO vo = accountService.openAccount(1L);

        assertThat(vo.getUserId()).isEqualTo(1L);
        assertThat(vo.getAccountNo()).startsWith("ACC");
        assertThat(vo.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(accountMapper).selectByUserId(1L);
        verify(accountMapper).insert(any(TxAccount.class));
    }

    @Test
    @DisplayName("重复开户 - 抛出 BizException")
    void openAccount_duplicate_throws() {
        when(accountMapper.selectByUserId(1L)).thenReturn(new TxAccount());

        assertThatThrownBy(() -> accountService.openAccount(1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("已存在");
    }

    @Test
    @DisplayName("充值成功 - 余额增加")
    void recharge_success() {
        TxAccount account = new TxAccount();
        account.setId(1L);
        account.setBalance(BigDecimal.valueOf(100));
        when(accountMapper.selectById(1L)).thenReturn(account);
        when(accountMapper.updateById(any())).thenReturn(1);

        TxAccountVO vo = accountService.recharge(1L, BigDecimal.valueOf(50));

        assertThat(vo.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(150));
        verify(accountMapper).selectById(1L);
        verify(accountMapper).updateById(any());
    }

    @Test
    @DisplayName("充值金额为 null - 抛出 BizException")
    void recharge_null_throws() {
        assertThatThrownBy(() -> accountService.recharge(1L, null))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("正数");
    }

    @Test
    @DisplayName("充值金额为负 - 抛出 BizException")
    void recharge_negative_throws() {
        assertThatThrownBy(() -> accountService.recharge(1L, BigDecimal.valueOf(-10)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("正数");
    }

    @Test
    @DisplayName("查询存在账户 - 返回 VO")
    void getById_found() {
        TxAccount account = new TxAccount();
        account.setId(1L);
        account.setUserId(1L);
        account.setAccountNo("ACC10001");
        account.setBalance(BigDecimal.TEN);
        when(accountMapper.selectById(1L)).thenReturn(account);

        TxAccountVO vo = accountService.getById(1L);

        assertThat(vo.getAccountNo()).isEqualTo("ACC10001");
        assertThat(vo.getBalance()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    @DisplayName("查询不存在账户 - 抛出 BizException")
    void getById_notFound_throws() {
        when(accountMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> accountService.getById(99L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不存在");
    }
}
