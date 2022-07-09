package com.example.gateway.server.message.internal;

import com.example.gateway.server.message.Header;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class InternalHeader extends Header {
    private Integer userId;

    public InternalHeader() {
    }

    public InternalHeader(short commandId, Integer userId) {
        super(commandId);
        this.userId = userId;
    }

    public Integer userId() {
        return this.userId;
    }

    @Override
    public ByteBuf toByteBuf() {
        ByteBuf buf = super.toByteBuf();
        buf.writeInt(userId);

        return buf;
    }
}
