package com.wuyou.onlytest.netty.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtocolServer {

    private final int port;
    private final ObjectMapper objectMapper;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    @Getter
    private String lastReceivedMessage;

    public ProtocolServer(int port, ObjectMapper objectMapper) {
        this.port = port;
        this.objectMapper = objectMapper;
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
                                .addLast(new CustomProtocolDecoder(objectMapper))
                                .addLast(new CustomProtocolEncoder(objectMapper))
                                .addLast(new SimpleChannelInboundHandler<CustomProtocol>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, CustomProtocol msg) {
                                        log.info("Protocol server received: type={}, content={}", msg.getType(), msg.getContent());
                                        lastReceivedMessage = "type=" + msg.getType() + ", content=" + msg.getContent();
                                        ctx.writeAndFlush(new CustomProtocol("echo", "received: " + msg.getContent()));
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                        log.error("Protocol server error", cause);
                                        ctx.close();
                                    }
                                });
                    }
                });
        channel = b.bind(port).sync().channel();
        log.info("Protocol server started on port {}", port);
    }

    public void stop() {
        if (channel != null) channel.close();
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        log.info("Protocol server stopped");
    }

    public boolean isRunning() {
        return channel != null && channel.isActive();
    }
}
