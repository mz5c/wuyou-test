package com.wuyou.onlytest.seata.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.seata.service.SeataTccService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "Seata TCC 模式")
@RestController
@RequestMapping("/api/v1/seata/tcc")
@RequiredArgsConstructor
public class SeataTccController {

    private final SeataTccService seataTccService;

    @Operation(summary = "TCC 账户转账")
    @PostMapping("/transfer")
    public Result<String> transfer(@RequestParam Long fromUserId,
                                   @RequestParam Long toUserId,
                                   @RequestParam BigDecimal amount) {
        seataTccService.tryTransfer(fromUserId, toUserId, amount);
        return Result.success("transfer success");
    }
}
