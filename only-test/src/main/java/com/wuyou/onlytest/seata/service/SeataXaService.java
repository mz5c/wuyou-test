package com.wuyou.onlytest.seata.service;

import com.wuyou.common.exception.BizException;
import com.wuyou.common.result.ResultCode;
import com.wuyou.onlytest.entity.demo.Order;
import com.wuyou.onlytest.entity.demo.OrderItem;
import com.wuyou.onlytest.entity.demo.Product;
import com.wuyou.onlytest.mapper.demo.OrderItemMapper;
import com.wuyou.onlytest.mapper.demo.OrderMapper;
import com.wuyou.onlytest.mapper.demo.ProductMapper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeataXaService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    @GlobalTransactional(name = "create-order-xa", rollbackFor = Exception.class)
    public Order createOrder(Long userId, Long productId, int quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStock() < quantity) {
            throw new BizException(ResultCode.BIZ_ERROR, "insufficient stock");
        }
        product.setStock(product.getStock() - quantity);
        productMapper.updateById(product);

        Order order = new Order();
        order.setOrderNo("SEATA_XA_" + System.currentTimeMillis());
        order.setUserId(userId);
        order.setStatus(0);
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        orderMapper.insert(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductName(product.getName());
        item.setQuantity(quantity);
        item.setPrice(product.getPrice());
        orderItemMapper.insertBatchXml(List.of(item));

        return order;
    }
}
