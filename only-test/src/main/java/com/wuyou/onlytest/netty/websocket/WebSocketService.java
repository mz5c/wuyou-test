package com.wuyou.onlytest.netty.websocket;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebSocketService {

    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public void add(Channel channel) {
        channels.add(channel);
        log.info("WebSocket client connected: {}, total clients: {}", channel.remoteAddress(), channels.size());
    }

    public void remove(Channel channel) {
        channels.remove(channel);
        log.info("WebSocket client disconnected: {}, total clients: {}", channel.remoteAddress(), channels.size());
    }

    public int broadcast(String message) {
        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        channels.writeAndFlush(frame);
        int count = channels.size();
        log.info("Broadcast message to {} clients: {}", count, message);
        return count;
    }

    public int clientCount() {
        return channels.size();
    }
}
