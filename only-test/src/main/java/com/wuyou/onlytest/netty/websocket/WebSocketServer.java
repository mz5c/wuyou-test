package com.wuyou.onlytest.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketServer {

    private final int port;
    private final WebSocketService webSocketService;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    public WebSocketServer(int port, WebSocketService webSocketService) {
        this.port = port;
        this.webSocketService = webSocketService;
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(new WebSocketServerProtocolHandler("/ws"))
                                .addLast(new SimpleChannelInboundHandler<TextWebSocketFrame>() {
                                    @Override
                                    public void handlerAdded(ChannelHandlerContext ctx) {
                                        webSocketService.add(ctx.channel());
                                    }

                                    @Override
                                    public void handlerRemoved(ChannelHandlerContext ctx) {
                                        webSocketService.remove(ctx.channel());
                                    }

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
                                        String msg = frame.text();
                                        log.info("WebSocket received: {}", msg);
                                        ctx.writeAndFlush(new TextWebSocketFrame("SERVER: " + msg));
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                        log.error("WebSocket error", cause);
                                        ctx.close();
                                    }
                                });
                    }
                });
        channel = b.bind(port).sync().channel();
        log.info("WebSocket server started on port {}, ws://127.0.0.1:{}/ws", port, port);
    }

    public void stop() {
        if (channel != null) channel.close();
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        log.info("WebSocket server stopped");
    }

    public boolean isRunning() {
        return channel != null && channel.isActive();
    }
}
