package com.wuyou.onlytest.netty.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProtocolClient {

    private final int port;
    private final ObjectMapper objectMapper;

    public ProtocolClient(int port, ObjectMapper objectMapper) {
        this.port = port;
        this.objectMapper = objectMapper;
    }

    public String sendAndReceive(CustomProtocol request) throws Exception {
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
                            ch.pipeline()
                                    .addLast(new CustomProtocolDecoder(objectMapper))
                                    .addLast(new CustomProtocolEncoder(objectMapper))
                                    .addLast(new SimpleChannelInboundHandler<CustomProtocol>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, CustomProtocol msg) {
                                            result[0] = "type=" + msg.getType() + ", content=" + msg.getContent();
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
            f.channel().writeAndFlush(request).sync();

            if (!latch.await(10, TimeUnit.SECONDS)) {
                return "TIMEOUT: no response";
            }
            return result[0];
        } finally {
            group.shutdownGracefully();
        }
    }
}
