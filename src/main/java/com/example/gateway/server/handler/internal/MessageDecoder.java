package com.example.gateway.server.handler.internal;

import com.example.gateway.server.handler.MessageConstants;
import com.example.gateway.server.message.internal.InternalHeader;
import com.example.gateway.server.message.internal.InternalMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        log.debug("received message, length :  {}", in.readableBytes());
        if (in.readableBytes() < MessageConstants.INTERNAL_PROTOCOL_HEADER_LEN) {
            return;
        }
        InternalHeader header = readHeader(in);
        if (header == null) {
            log.error("parse header failed!");
            return;
        }

        log.debug("receive message: {} {}", header.commandId(), header.length());

        ByteBuf dataBuffer = Unpooled.buffer(header.length());
        in.readBytes(dataBuffer, header.length());

        InternalMessage message = new InternalMessage();
        message.setHeader(header);
        message.setPayload(dataBuffer);

        out.add(message);
    }

    private InternalHeader readHeader(ByteBuf in) {
        in.markReaderIndex();

        InternalHeader header = new InternalHeader();
        header.setLength(in.readShort());
        header.setCommandId(in.readShort());
        header.setUserId(in.readInt());

        if (in.readableBytes() < header.length()) {
            log.info("readable bytes is not enough, rollback! {} {}", in.readableBytes(), header.length());
            in.resetReaderIndex();
            return null;
        }

        return header;
    }
}
