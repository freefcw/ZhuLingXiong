package com.example.gateway.server.handler.internal;

import com.example.gateway.server.message.internal.InternalMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class MessageEncoder extends MessageToByteEncoder<InternalMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, InternalMessage message, ByteBuf byteBuf) {
        ByteBuf byteBuf1 = message.toByteBuf();
        if (byteBuf1 == null) {
            log.error("message {} has error, skipped!", message.header().commandId());
            return;
        }
        byteBuf.writeBytes(byteBuf1);
        ReferenceCountUtil.release(byteBuf1);
    }
}
