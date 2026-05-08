package com.wuyou.onlytest.controller.demo;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.entity.demo.Order;
import com.wuyou.onlytest.entity.demo.OrderItem;
import com.wuyou.onlytest.service.demo.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}/with-items")
    public Result<Order> getWithItems(@PathVariable Long id) {
        return Result.success(orderService.getOrderWithItems(id));
    }

    @PostMapping
    public Result<Order> create(@RequestParam Long userId, @RequestBody List<OrderItem> items) {
        return Result.success(orderService.createOrder(userId, items));
    }

    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.payOrder(id);
        return Result.success(null);
    }
}
