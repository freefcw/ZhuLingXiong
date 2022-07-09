package com.example.gateway.server.message;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class Message {
    private ByteBuf payload;

    public abstract short commandId();

    public abstract Header header();

    public void setPayload(MessageLite payload) {
        this.payload = Unpooled.buffer().writeBytes(payload.toByteArray());
    }

    public ByteBuf payload() {
        return this.payload;
    }

    public void setPayload(ByteBuf byteBuf) {
        this.payload = byteBuf;
    }

    public ByteBuf toByteBuf() {
        if (this.payload == null) {
            return null;
        }
        this.header().setLength((short) this.payload.readableBytes());

        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(this.header().toByteBuf());
        byteBuf.writeBytes(this.payload);

        return byteBuf;
    }
}
