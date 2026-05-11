package com.wuyou.onlytest.seata.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.entity.demo.Order;
import com.wuyou.onlytest.seata.service.SeataXaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Seata XA 模式")
@RestController
@RequestMapping("/api/v1/seata/xa")
@RequiredArgsConstructor
public class SeataXaController {

    private final SeataXaService seataXaService;

    @Operation(summary = "XA 模式创建订单（与 AT 同场景，对比效果）")
    @PostMapping("/order")
    public Result<Order> createOrder(@RequestParam Long userId,
                                     @RequestParam Long productId,
                                     @RequestParam int quantity) {
        return Result.success(seataXaService.createOrder(userId, productId, quantity));
    }
}
