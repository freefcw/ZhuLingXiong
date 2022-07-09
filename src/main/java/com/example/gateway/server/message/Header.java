package com.example.gateway.server.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Header {
    private short length;
    private short commandId;

    public Header() {
    }

    public Header(short commandId) {
        this.commandId = commandId;
    }

    public short commandId() {
        return this.commandId;
    }

    public void setLength(short length) {
        this.length = length;
    }

    public short length() {
        return this.length;
    }

    public void setCommandId(short commandId) {
        this.commandId = commandId;
    }

    public ByteBuf toByteBuf() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeShort(this.length());
        buf.writeShort(this.commandId());
//        buf.writeShortLE(this.length()+2);
//        buf.writeShortLE(this.commandId());

        return buf;
    }

    @Override
    public String toString() {
        return "HEADER[id=" + this.commandId + "]";
    }
}
