package com.wuyou.onlytest.netty.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuyou.onlytest.netty.echo.EchoClient;
import com.wuyou.onlytest.netty.echo.EchoServer;
import com.wuyou.onlytest.netty.heartbeat.HeartbeatServer;
import com.wuyou.onlytest.netty.http.NettyHttpServer;
import com.wuyou.onlytest.netty.protocol.CustomProtocol;
import com.wuyou.onlytest.netty.protocol.ProtocolClient;
import com.wuyou.onlytest.netty.protocol.ProtocolServer;
import com.wuyou.onlytest.netty.websocket.WebSocketServer;
import com.wuyou.onlytest.netty.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NettyServerManager {

    private final ObjectMapper objectMapper;
    private final WebSocketService webSocketService;

    private EchoServer echoServer;
    private ProtocolServer protocolServer;
    private NettyHttpServer httpServer;
    private WebSocketServer webSocketServer;
    private HeartbeatServer heartbeatServer;

    public static final int ECHO_PORT = 8999;
    public static final int PROTOCOL_PORT = 9000;
    public static final int HTTP_PORT = 9001;
    public static final int WS_PORT = 9002;
    public static final int HEARTBEAT_PORT = 9003;

    public synchronized String startEcho() throws InterruptedException {
        if (echoServer != null && echoServer.isRunning()) {
            return "Echo server already running on port " + ECHO_PORT;
        }
        echoServer = new EchoServer(ECHO_PORT);
        echoServer.start();
        return "Echo server started on port " + ECHO_PORT;
    }

    public synchronized String stopEcho() {
        if (echoServer == null) return "Echo server not running";
        echoServer.stop();
        echoServer = null;
        return "Echo server stopped";
    }

    public String sendEcho(String msg) throws Exception {
        if (echoServer == null || !echoServer.isRunning()) {
            return "ERROR: Echo server not running";
        }
        EchoClient client = new EchoClient(ECHO_PORT);
        return client.sendMessage(msg);
    }

    public synchronized String startProtocol() throws InterruptedException {
        if (protocolServer != null && protocolServer.isRunning()) {
            return "Protocol server already running on port " + PROTOCOL_PORT;
        }
        protocolServer = new ProtocolServer(PROTOCOL_PORT, objectMapper);
        protocolServer.start();
        return "Protocol server started on port " + PROTOCOL_PORT;
    }

    public synchronized String stopProtocol() {
        if (protocolServer == null) return "Protocol server not running";
        protocolServer.stop();
        protocolServer = null;
        return "Protocol server stopped";
    }

    public String sendProtocol(String type, String content) throws Exception {
        if (protocolServer == null || !protocolServer.isRunning()) {
            return "ERROR: Protocol server not running";
        }
        ProtocolClient client = new ProtocolClient(PROTOCOL_PORT, objectMapper);
        return client.sendAndReceive(new CustomProtocol(type, content));
    }

    public synchronized String startHttp() throws InterruptedException {
        if (httpServer != null && httpServer.isRunning()) {
            return "HTTP server already running on port " + HTTP_PORT;
        }
        httpServer = new NettyHttpServer(HTTP_PORT);
        httpServer.start();
        return "HTTP server started on port " + HTTP_PORT;
    }

    public synchronized String stopHttp() {
        if (httpServer == null) return "HTTP server not running";
        httpServer.stop();
        httpServer = null;
        return "HTTP server stopped";
    }

    public synchronized String startWebSocket() throws InterruptedException {
        if (webSocketServer != null && webSocketServer.isRunning()) {
            return "WebSocket server already running on port " + WS_PORT;
        }
        webSocketServer = new WebSocketServer(WS_PORT, webSocketService);
        webSocketServer.start();
        return "WebSocket server started on port " + WS_PORT + ", path=/ws";
    }

    public synchronized String stopWebSocket() {
        if (webSocketServer == null) return "WebSocket server not running";
        webSocketServer.stop();
        webSocketServer = null;
        return "WebSocket server stopped";
    }

    public synchronized String startHeartbeat() throws InterruptedException {
        if (heartbeatServer != null && heartbeatServer.isRunning()) {
            return "Heartbeat server already running on port " + HEARTBEAT_PORT;
        }
        heartbeatServer = new HeartbeatServer(HEARTBEAT_PORT);
        heartbeatServer.start();
        return "Heartbeat server started on port " + HEARTBEAT_PORT + ", idle timeout=5s";
    }

    public synchronized String stopHeartbeat() {
        if (heartbeatServer == null) return "Heartbeat server not running";
        heartbeatServer.stop();
        heartbeatServer = null;
        return "Heartbeat server stopped";
    }

    public Map<String, Object> status() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("echo", echoServer != null && echoServer.isRunning());
        map.put("protocol", protocolServer != null && protocolServer.isRunning());
        map.put("http", httpServer != null && httpServer.isRunning());
        map.put("websocket", webSocketServer != null && webSocketServer.isRunning());
        map.put("heartbeat", heartbeatServer != null && heartbeatServer.isRunning());
        return map;
    }

    @PreDestroy
    public void shutdownAll() {
        log.info("Shutting down all Netty servers...");
        stopEcho();
        stopProtocol();
        stopHttp();
        stopWebSocket();
        stopHeartbeat();
    }
}
