package com.wuyou.onlytest.netty.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class CustomProtocolDecoder extends LengthFieldBasedFrameDecoder {

    private static final int MAX_FRAME_LENGTH = 1024 * 1024;
    private final ObjectMapper objectMapper;

    public CustomProtocolDecoder(ObjectMapper objectMapper) {
        super(MAX_FRAME_LENGTH, 0, 4, 0, 4);
        this.objectMapper = objectMapper;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        try {
            byte[] bytes = new byte[frame.readableBytes()];
            frame.readBytes(bytes);
            return objectMapper.readValue(bytes, CustomProtocol.class);
        } finally {
            frame.release();
        }
    }
}
