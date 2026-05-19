package com.wuyou.onlytest.tx.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuyou.common.result.Result;
import com.wuyou.onlytest.tx.dto.TransferRequest;
import com.wuyou.onlytest.tx.dto.TransferVO;
import com.wuyou.onlytest.tx.entity.TransferRecord;
import com.wuyou.onlytest.tx.service.TransferService;
import com.wuyou.onlytest.tx.mapper.TransferRecordMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "交易沙箱-转账")
@RestController
@RequestMapping("/api/v1/tx")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final TransferRecordMapper recordMapper;

    @Operation(summary = "基础转账（REQUIRED + FOR UPDATE）")
    @PostMapping("/transfer")
    public Result<TransferVO> transfer(@Valid @RequestBody TransferRequest req) {
        return Result.success(TransferVO.from(transferService.transfer(req)));
    }

    @Operation(summary = "悲观锁转账")
    @PostMapping("/transfer/pessimistic")
    public Result<TransferVO> transferPessimistic(@Valid @RequestBody TransferRequest req) {
        return Result.success(TransferVO.from(transferService.transferPessimistic(req)));
    }

    @Operation(summary = "乐观锁转账")
    @PostMapping("/transfer/optimistic")
    public Result<TransferVO> transferOptimistic(@Valid @RequestBody TransferRequest req) {
        return Result.success(TransferVO.from(transferService.transferOptimistic(req)));
    }

    @Operation(summary = "分布式锁转账（Redisson）")
    @PostMapping("/transfer/distributed-lock")
    public Result<TransferVO> transferWithLock(@Valid @RequestBody TransferRequest req) {
        return Result.success(TransferVO.from(transferService.transferWithLock(req)));
    }

    @Operation(summary = "查询转账记录（分页）")
    @GetMapping("/records")
    public Result<?> listRecords(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size,
                                 @RequestParam(required = false) String accountNo) {
        Page<TransferRecord> p = new Page<>(page, size);
        if (accountNo != null) {
            return Result.success(recordMapper.selectPageByAccountNo(p, accountNo));
        }
        return Result.success(recordMapper.selectPage(p, null));
    }
}
