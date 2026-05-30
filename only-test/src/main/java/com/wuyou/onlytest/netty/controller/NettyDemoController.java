package com.wuyou.onlytest.netty.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.netty.manager.NettyServerManager;
import com.wuyou.onlytest.netty.websocket.WebSocketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Netty 网络编程测试")
@RestController
@RequestMapping("/api/v1/netty")
@RequiredArgsConstructor
public class NettyDemoController {

    private final NettyServerManager serverManager;
    private final WebSocketService webSocketService;

    // ========== 服务器状态 ==========

    @Operation(summary = "查询所有 Netty 服务器状态")
    @GetMapping("/servers")
    public Result<Map<String, Object>> listServers() {
        return Result.success(serverManager.status());
    }

    // ========== Echo ==========

    @Operation(summary = "Echo - 启动 TCP 回显服务器 (端口 8999)")
    @PostMapping("/echo/start")
    public Result<String> startEcho() throws InterruptedException {
        return Result.success(serverManager.startEcho());
    }

    @Operation(summary = "Echo - 停止服务器")
    @PostMapping("/echo/stop")
    public Result<String> stopEcho() {
        return Result.success(serverManager.stopEcho());
    }

    @Operation(summary = "Echo - 发送消息并接收回显")
    @PostMapping("/echo/send")
    public Result<String> sendEcho(@RequestParam String msg) throws Exception {
        return Result.success(serverManager.sendEcho(msg));
    }

    // ========== Protocol ==========

    @Operation(summary = "Protocol - 启动自定义协议服务器 (端口 9000)")
    @PostMapping("/protocol/start")
    public Result<String> startProtocol() throws InterruptedException {
        return Result.success(serverManager.startProtocol());
    }

    @Operation(summary = "Protocol - 停止服务器")
    @PostMapping("/protocol/stop")
    public Result<String> stopProtocol() {
        return Result.success(serverManager.stopProtocol());
    }

    @Operation(summary = "Protocol - 发送编码消息（演示 LengthFieldBasedFrameDecoder）")
    @PostMapping("/protocol/send")
    public Result<String> sendProtocol(@RequestParam(defaultValue = "greeting") String type,
                                       @RequestParam(defaultValue = "Hello Netty") String content) throws Exception {
        return Result.success(serverManager.sendProtocol(type, content));
    }

    // ========== HTTP ==========

    @Operation(summary = "HTTP - 启动 Netty HTTP 服务器 (端口 9001)")
    @PostMapping("/http/start")
    public Result<String> startHttp() throws InterruptedException {
        return Result.success(serverManager.startHttp());
    }

    @Operation(summary = "HTTP - 停止服务器")
    @PostMapping("/http/stop")
    public Result<String> stopHttp() {
        return Result.success(serverManager.stopHttp());
    }

    // ========== WebSocket ==========

    @Operation(summary = "WebSocket - 启动 WebSocket 服务器 (端口 9002, path=/ws)")
    @PostMapping("/ws/start")
    public Result<String> startWebSocket() throws InterruptedException {
        return Result.success(serverManager.startWebSocket());
    }

    @Operation(summary = "WebSocket - 停止服务器")
    @PostMapping("/ws/stop")
    public Result<String> stopWebSocket() {
        return Result.success(serverManager.stopWebSocket());
    }

    @Operation(summary = "WebSocket - 广播消息给所有连接的客户端")
    @PostMapping("/ws/broadcast")
    public Result<String> broadcast(@RequestParam String message) {
        int count = webSocketService.broadcast(message);
        return Result.success("Broadcast to " + count + " clients: " + message);
    }

    @Operation(summary = "WebSocket - 查看在线客户端数")
    @GetMapping("/ws/clients")
    public Result<Integer> clientCount() {
        return Result.success(webSocketService.clientCount());
    }

    // ========== Heartbeat ==========

    @Operation(summary = "Heartbeat - 启动心跳检测服务器 (端口 9003, 5s 空闲超时)")
    @PostMapping("/heartbeat/start")
    public Result<String> startHeartbeat() throws InterruptedException {
        return Result.success(serverManager.startHeartbeat());
    }

    @Operation(summary = "Heartbeat - 停止服务器")
    @PostMapping("/heartbeat/stop")
    public Result<String> stopHeartbeat() {
        return Result.success(serverManager.stopHeartbeat());
    }
}
