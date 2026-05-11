package com.wuyou.onlytest.seata.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.entity.demo.Order;
import com.wuyou.onlytest.seata.service.SeataAtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Seata AT 模式")
@RestController
@RequestMapping("/api/v1/seata/at")
@RequiredArgsConstructor
public class SeataAtController {

    private final SeataAtService seataAtService;

    @Operation(summary = "AT 模式创建订单（扣库存+下单在一个全局事务）")
    @PostMapping("/order")
    public Result<Order> createOrder(@RequestParam Long userId,
                                     @RequestParam Long productId,
                                     @RequestParam int quantity) {
        return Result.success(seataAtService.createOrder(userId, productId, quantity));
    }
}
