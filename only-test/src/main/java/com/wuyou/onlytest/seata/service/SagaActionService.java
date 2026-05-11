package com.wuyou.onlytest.seata.service;

import com.wuyou.onlytest.entity.demo.Product;
import com.wuyou.onlytest.entity.demo.User;
import com.wuyou.onlytest.mapper.demo.ProductMapper;
import com.wuyou.onlytest.mapper.demo.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaActionService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public SagaResult createOrder(Long userId, Long productId, int quantity) {
        log.info("Saga createOrder: userId={}, productId={}, quantity={}", userId, productId, quantity);
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("product not found: " + productId);
        }
        // 模拟创建订单
        SagaResult result = new SagaResult();
        result.setOrderId(System.currentTimeMillis());
        result.setSuccess(true);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public SagaResult cancelOrder(Long orderId) {
        log.info("Saga cancelOrder: orderId={}", orderId);
        SagaResult result = new SagaResult();
        result.setSuccess(true);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public SagaResult deductStock(Long productId, int quantity) {
        log.info("Saga deductStock: productId={}, quantity={}", productId, quantity);
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("product not found: " + productId);
        }
        product.setStock(product.getStock() - quantity);
        productMapper.updateById(product);
        SagaResult result = new SagaResult();
        result.setSuccess(true);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public SagaResult addStock(Long productId, int quantity) {
        log.info("Saga addStock: productId={}, quantity={}", productId, quantity);
        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.setStock(product.getStock() + quantity);
            productMapper.updateById(product);
        }
        SagaResult result = new SagaResult();
        result.setSuccess(true);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public SagaResult deductBalance(Long userId, Long productId, int quantity) {
        log.info("Saga deductBalance: userId={}, productId={}, quantity={}", userId, productId, quantity);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("user not found: " + userId);
        }
        // 模拟扣款: 每件商品扣 100
        SagaResult result = new SagaResult();
        result.setSuccess(true);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public SagaResult addBalance(Long userId, Long productId, int quantity) {
        log.info("Saga addBalance: userId={}, productId={}, quantity={}", userId, productId, quantity);
        SagaResult result = new SagaResult();
        result.setSuccess(true);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public SagaResult notify(Long userId, Long orderId) {
        log.info("Saga notify: userId={}, orderId={}", userId, orderId);
        SagaResult result = new SagaResult();
        result.setSuccess(true);
        return result;
    }

    public static class SagaResult {
        private Long orderId;
        private boolean success;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
}
