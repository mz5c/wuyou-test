package com.wuyou.onlytest.tx.service;

import com.wuyou.onlytest.tx.dto.SplitRequest;
import com.wuyou.onlytest.tx.dto.SplitResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TxCalculationServiceTest {

    private final TxCalculationService service = new TxCalculationService();

    @Test
    @DisplayName("等额分摊 - 10.00 分 3 份，尾差归到最后")
    void split_equalWithRemainder() {
        SplitRequest req = new SplitRequest();
        req.setTotalAmount(BigDecimal.valueOf(10.00));
        req.setRatios(List.of(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE));
        req.setScale(2);
        req.setRoundingMode(RoundingMode.HALF_EVEN);

        SplitResult result = service.split(req);

        assertThat(result.getParts()).containsExactly(
                new BigDecimal("3.33"),
                new BigDecimal("3.33"),
                new BigDecimal("3.34"));
        assertThat(result.getSum()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("按比例分摊 - 100 按 50:30:20")
    void split_byRatio() {
        SplitRequest req = new SplitRequest();
        req.setTotalAmount(BigDecimal.valueOf(100));
        req.setRatios(List.of(BigDecimal.valueOf(50), BigDecimal.valueOf(30), BigDecimal.valueOf(20)));
        req.setScale(2);

        SplitResult result = service.split(req);

        assertThat(result.getParts()).containsExactly(
                new BigDecimal("50.00"),
                new BigDecimal("30.00"),
                new BigDecimal("20.00"));
        assertThat(result.getSum()).isEqualByComparingTo(new BigDecimal("100"));
    }

    @Test
    @DisplayName("0.01 分给两个人，尾差处理")
    void split_oneCentToTwo() {
        SplitRequest req = new SplitRequest();
        req.setTotalAmount(BigDecimal.valueOf(0.01));
        req.setRatios(List.of(BigDecimal.ONE, BigDecimal.ONE));
        req.setScale(2);

        SplitResult result = service.split(req);

        assertThat(result.getParts()).containsExactly(
                new BigDecimal("0.00"),
                new BigDecimal("0.01"));
        assertThat(result.getSum()).isEqualByComparingTo(new BigDecimal("0.01"));
    }

    @Test
    @DisplayName("空比例列表 - 抛出异常")
    void split_emptyRatios_throws() {
        SplitRequest req = new SplitRequest();
        req.setTotalAmount(BigDecimal.TEN);
        req.setRatios(List.of());

        assertThatThrownBy(() -> service.split(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("全零比例 - 抛出异常")
    void split_zeroRatios_throws() {
        SplitRequest req = new SplitRequest();
        req.setTotalAmount(BigDecimal.TEN);
        req.setRatios(List.of(BigDecimal.ZERO, BigDecimal.ZERO));

        assertThatThrownBy(() -> service.split(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("大于零");
    }

    @Test
    @DisplayName("负数比例 - 抛出异常")
    void split_negativeRatio_throws() {
        SplitRequest req = new SplitRequest();
        req.setTotalAmount(BigDecimal.TEN);
        req.setRatios(List.of(BigDecimal.ONE, BigDecimal.valueOf(-1)));

        assertThatThrownBy(() -> service.split(req))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
