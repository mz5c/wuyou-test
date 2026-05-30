package com.wuyou.onlytest.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.*;

@Slf4j
public class NettyHttpServer {

    private final int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    public NettyHttpServer(int port) {
        this.port = port;
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
                                .addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
                                        String uri = req.uri();
                                        String body = req.content().toString(StandardCharsets.UTF_8);
                                        log.info("HTTP request: {} {} body={}", req.method(), uri, body);

                                        String responseBody = "{\"method\":\"" + req.method() + "\",\"uri\":\"" + uri
                                                + "\",\"body\":\"" + (body.isEmpty() ? "" : body) + "\",\"from\":\"netty-http\"}";

                                        FullHttpResponse resp = new DefaultFullHttpResponse(
                                                HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                                                Unpooled.copiedBuffer(responseBody, StandardCharsets.UTF_8));
                                        resp.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
                                        resp.headers().set(CONTENT_LENGTH, resp.content().readableBytes());
                                        ctx.writeAndFlush(resp);
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                        log.error("HTTP server error", cause);
                                        ctx.close();
                                    }
                                });
                    }
                });
        channel = b.bind(port).sync().channel();
        log.info("Netty HTTP server started on port {}", port);
    }

    public void stop() {
        if (channel != null) channel.close();
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        log.info("Netty HTTP server stopped");
    }

    public boolean isRunning() {
        return channel != null && channel.isActive();
    }
}
