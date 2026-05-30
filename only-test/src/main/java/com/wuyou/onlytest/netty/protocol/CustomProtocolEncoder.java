package com.wuyou.onlytest.netty.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class CustomProtocolEncoder extends MessageToByteEncoder<CustomProtocol> {

    private final ObjectMapper objectMapper;

    @Override
    protected void encode(ChannelHandlerContext ctx, CustomProtocol msg, ByteBuf out) throws Exception {
        byte[] body = objectMapper.writeValueAsBytes(msg);
        out.writeInt(body.length);
        out.writeBytes(body);
    }
}
