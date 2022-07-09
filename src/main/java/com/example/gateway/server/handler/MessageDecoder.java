package com.example.gateway.server.handler;

import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.message.Header;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * message decode can custom
 */
@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        log.debug("received message, length :  {}", in.readableBytes());
        if (in.readableBytes() < MessageConstants.PROTOCOL_HEADER_LEN) {
            return;
        }
        Header header = readHeader(in);
        if (header == null) {
            log.error("parse header failed!");
            return;
        }

        log.debug("receive message: {} {}", header.commandId(), header.length());

        ByteBuf dataBuffer = Unpooled.buffer(header.length());
        in.readBytes(dataBuffer, header.length());

        BaseMessage message = new BaseMessage();
        message.setHeader(header);
        message.setPayload(dataBuffer);

        out.add(message);
    }

    private Header readHeader(ByteBuf in) {
        // message structure
        // short: data length
        // short: command
        // data
        in.markReaderIndex();

        Header header = new Header();
        header.setLength(in.readShort());
        header.setCommandId(in.readShort());

        if (in.readableBytes() < header.length()) {
            log.info("readable bytes is not enough, rollback! {} {}", in.readableBytes(), header.length());
            in.resetReaderIndex();
            return null;
        }

        return header;
    }
}
