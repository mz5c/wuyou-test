package com.wuyou.onlytest.seata.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.seata.service.SeataSagaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Seata Saga 模式")
@RestController
@RequestMapping("/api/v1/seata/saga")
@RequiredArgsConstructor
public class SeataSagaController {

    private final SeataSagaService seataSagaService;

    @Operation(summary = "Saga 模式创建订单（可补偿）")
    @PostMapping("/order")
    public Result<String> createOrder(@RequestParam Long userId,
                                      @RequestParam Long productId,
                                      @RequestParam int quantity) {
        return Result.success(seataSagaService.createOrderBySaga(userId, productId, quantity));
    }
}
