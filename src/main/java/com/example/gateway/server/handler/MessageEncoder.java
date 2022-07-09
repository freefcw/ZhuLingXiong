package com.example.gateway.server.handler;

import com.example.gateway.server.message.BaseMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class MessageEncoder extends MessageToByteEncoder<BaseMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, BaseMessage message, ByteBuf byteBuf) {
        ByteBuf byteBuf1 = message.toByteBuf();
        if (byteBuf1 == null) {
            log.error("message {} has error, skipped!", message.header().commandId());
            return;
        }
        byteBuf.writeBytes(byteBuf1);
        ReferenceCountUtil.release(byteBuf1);
    }
}
