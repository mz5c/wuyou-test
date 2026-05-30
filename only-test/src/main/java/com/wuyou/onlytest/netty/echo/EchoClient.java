package com.wuyou.onlytest.netty.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class EchoClient {

    private final int port;

    public EchoClient(int port) {
        this.port = port;
    }

    public String sendMessage(String message) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            String[] result = {null};
            CountDownLatch latch = new CountDownLatch(1);

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                    ByteBuf buf = (ByteBuf) msg;
                                    result[0] = buf.toString(StandardCharsets.UTF_8);
                                    buf.release();
                                    latch.countDown();
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    result[0] = "ERROR: " + cause.getMessage();
                                    latch.countDown();
                                }
                            });
                        }
                    });

            ChannelFuture f = b.connect("127.0.0.1", port).sync();
            f.channel().writeAndFlush(Unpooled.copiedBuffer(message, StandardCharsets.UTF_8)).sync();

            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new TimeoutException("Echo timeout after 10s");
            }
            return result[0];
        } finally {
            group.shutdownGracefully();
        }
    }
}
