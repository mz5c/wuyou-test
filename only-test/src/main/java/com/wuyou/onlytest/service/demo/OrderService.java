package com.wuyou.onlytest.service.demo;

import com.wuyou.common.exception.BizException;
import com.wuyou.common.result.ResultCode;
import com.wuyou.onlytest.entity.demo.Order;
import com.wuyou.onlytest.entity.demo.OrderItem;
import com.wuyou.onlytest.mapper.demo.OrderItemMapper;
import com.wuyou.onlytest.mapper.demo.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public Order getOrderWithItems(Long orderId) {
        List<Order> orders = orderMapper.selectOrderWithItems(orderId);
        return orders.isEmpty() ? null : orders.get(0);
    }

    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, List<OrderItem> items) {
        Order order = new Order();
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setUserId(userId);
        order.setStatus(0);

        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        orderMapper.insert(order);

        items.forEach(item -> item.setOrderId(order.getId()));
//        orderItemMapper.insertBatch(items);
        orderItemMapper.insertBatchXml(items);

        return order;
    }




    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND, "order not found");
        }
        order.setStatus(1);
        orderMapper.updateById(order);

        deductStock(orderId);

        log.info("order {} paid successfully", orderId);
    }

    private void deductStock(Long orderId) {
        throw new BizException(ResultCode.BIZ_ERROR, "deduct stock failed, rollback");
    }
}
