package com.wuyou.onlytest.lock.service;

import com.wuyou.onlytest.entity.demo.Product;
import com.wuyou.onlytest.mapper.demo.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockDemoService {

    private final RedissonClient redissonClient;
    private final ProductMapper productMapper;

    public Product readProduct(Long productId) {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("lock:product:" + productId);
        rwLock.readLock().lock(5, TimeUnit.SECONDS);
        try {
            log.info("acquired read lock for product {}", productId);
            sleep(1000);
            return productMapper.selectById(productId);
        } finally {
            rwLock.readLock().unlock();
            log.info("released read lock for product {}", productId);
        }
    }

    public void writeProduct(Long productId, int stock) {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("lock:product:" + productId);
        rwLock.writeLock().lock(5, TimeUnit.SECONDS);
        try {
            log.info("acquired write lock for product {}", productId);
            Product product = productMapper.selectById(productId);
            if (product != null) {
                product.setStock(stock);
                productMapper.updateById(product);
            }
            sleep(1000);
        } finally {
            rwLock.writeLock().unlock();
            log.info("released write lock for product {}", productId);
        }
    }

    public boolean acquireSemaphore(String name, int permits) {
        RSemaphore semaphore = redissonClient.getSemaphore("semaphore:" + name);
        try {
            boolean acquired = semaphore.tryAcquire(permits, 3, TimeUnit.SECONDS);
            log.info("acquired semaphore {} permits {}: {}", name, permits, acquired);
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("interrupted while acquiring semaphore {} permits {}", name, permits);
            return false;
        }
    }

    public void releaseSemaphore(String name, int permits) {
        RSemaphore semaphore = redissonClient.getSemaphore("semaphore:" + name);
        semaphore.release(permits);
        log.info("released semaphore {} permits {}", name, permits);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
