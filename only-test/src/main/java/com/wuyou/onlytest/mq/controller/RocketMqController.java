package com.wuyou.onlytest.mq.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.mq.service.RocketMqProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "RocketMQ 消息测试")
@RestController
@RequestMapping("/api/v1/mq")
@RequiredArgsConstructor
public class RocketMqController {

    private final RocketMqProducerService producerService;

    @Operation(summary = "发送普通消息（同步）")
    @PostMapping("/send")
    public Result<String> send(@RequestParam(defaultValue = "demo-order") String topic,
                               @RequestParam String body,
                               @RequestParam(defaultValue = "sync") String mode) {
        switch (mode) {
            case "async":
                producerService.sendAsync(topic, body);
                return Result.success("async sent");
            case "oneway":
                producerService.sendOneWay(topic, body);
                return Result.success("oneway sent");
            default:
                return Result.success(producerService.sendSync(topic, body).toString());
        }
    }

    @Operation(summary = "发送顺序消息")
    @PostMapping("/send-orderly")
    public Result<String> sendOrderly(@RequestParam String body, @RequestParam String orderKey) {
        return Result.success(producerService.sendOrderly("demo-order", body, orderKey).toString());
    }

    @Operation(summary = "发送事务消息")
    @PostMapping("/send-transaction")
    public Result<String> sendTransaction(@RequestParam String body) {
        producerService.sendTransaction("demo-order-transaction", body);
        return Result.success("transaction msg sent");
    }
}
