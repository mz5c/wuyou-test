package com.wuyou.onlytest.service.demo;

import com.wuyou.common.exception.BizException;
import com.wuyou.common.result.ResultCode;
import com.wuyou.onlytest.entity.demo.Product;
import com.wuyou.onlytest.mapper.demo.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    private static final String CACHE_KEY_PREFIX = "wuyou-test:product:";

    @Cacheable(value = "product", key = "#id")
    public Product getById(Long id) {
        log.info("cache miss, query db for product {}", id);
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(ResultCode.NOT_FOUND, "product not found");
        }
        return product;
    }

    public Product getByIdManualCache(Long id) {
        String key = CACHE_KEY_PREFIX + id;
        Product product = (Product) redisTemplate.opsForValue().get(key);
        if (product != null) {
            log.info("cache hit: {}", key);
            return product;
        }
        product = productMapper.selectById(id);
        if (product != null) {
            redisTemplate.opsForValue().set(key, product, 30, TimeUnit.MINUTES);
        }
        return product;
    }

    public boolean updateById(Product product) {
        int rows = productMapper.updateById(product);
        log.info("product {} updated: {}", product.getId(), rows > 0);
        return rows > 0;
    }

    @CacheEvict(value = "product", key = "#id")
    public void evictCache(Long id) {
        log.info("cache evicted: product:{}", id);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long productId, int quantity) {
        RLock lock = redissonClient.getLock(CACHE_KEY_PREFIX + "lock:" + productId);
        boolean locked = false;
        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BizException(ResultCode.BIZ_ERROR, "try lock timeout");
            }
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new BizException(ResultCode.NOT_FOUND, "product not found");
            }
            if (product.getStock() < quantity) {
                throw new BizException(ResultCode.BIZ_ERROR, "insufficient stock");
            }
            product.setStock(product.getStock() - quantity);
            boolean updated = productMapper.updateById(product) > 0;
            if (updated) {
                evictCache(productId);
            }
            return updated;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(ResultCode.BIZ_ERROR, "lock interrupted");
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}
