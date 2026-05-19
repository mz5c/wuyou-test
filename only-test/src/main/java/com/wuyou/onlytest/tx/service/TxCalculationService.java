package com.wuyou.onlytest.tx.service;

import com.wuyou.onlytest.tx.dto.SplitRequest;
import com.wuyou.onlytest.tx.dto.SplitResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TxCalculationService {

    /**
     * 按比例分摊总金额，保证 sum(parts) == totalAmount
     */
    public SplitResult split(SplitRequest req) {
        List<BigDecimal> ratios = req.getRatios();
        if (ratios == null || ratios.isEmpty()) {
            throw new IllegalArgumentException("比例列表不能为空");
        }
        if (ratios.stream().anyMatch(r -> r.compareTo(BigDecimal.ZERO) < 0)) {
            throw new IllegalArgumentException("比例不能为负数");
        }

        BigDecimal total = req.getTotalAmount();
        int scale = req.getScale();
        RoundingMode mode = req.getRoundingMode();

        BigDecimal ratioSum = ratios.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (ratioSum.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("比例之和必须大于零");
        }

        List<BigDecimal> parts = new ArrayList<>(ratios.size());

        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < ratios.size(); i++) {
            if (i == ratios.size() - 1) {
                // 最后一份：总金额减去已分配金额，消除尾差
                parts.add(total.subtract(allocated).setScale(scale, mode));
            } else {
                BigDecimal part = total.multiply(ratios.get(i))
                        .divide(ratioSum, scale, mode);
                parts.add(part);
                allocated = allocated.add(part);
            }
        }

        BigDecimal sum = parts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("split: total={}, ratios={}, parts={}", total, ratios, parts);
        return new SplitResult(parts, sum, total);
    }
}
