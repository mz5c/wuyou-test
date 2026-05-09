package com.wuyou.onlytest.controller.demo;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.entity.demo.Order;
import com.wuyou.onlytest.entity.demo.OrderItem;
import com.wuyou.onlytest.service.demo.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单管理", description = "订单 CRUD 接口")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "查询订单及订单项")
    @GetMapping("/{id}/with-items")
    public Result<Order> getWithItems(@Parameter(description = "订单 ID") @PathVariable Long id) {
        return Result.success(orderService.getOrderWithItems(id));
    }

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<Order> create(@Parameter(description = "用户 ID", required = true) @RequestParam Long userId, 
                                @Parameter(description = "订单项列表", required = true) @RequestBody List<OrderItem> items) {
        return Result.success(orderService.createOrder(userId, items));
    }

    @Operation(summary = "支付订单")
    @PostMapping("/{id}/pay")
    public Result<Void> pay(@Parameter(description = "订单 ID") @PathVariable Long id) {
        orderService.payOrder(id);
        return Result.success(null);
    }
}
