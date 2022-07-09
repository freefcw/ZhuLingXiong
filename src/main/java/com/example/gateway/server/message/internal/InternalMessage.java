package com.example.gateway.server.message.internal;

import com.example.gateway.server.message.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InternalMessage extends Message {
    private InternalHeader header;

    public InternalHeader header() {
        return header;
    }

    public Integer userId() {
        if (this.header != null) {
            return this.header.userId();
        }
        return null;
    }

    public void setHeader(InternalHeader header) {
        this.header = header;
    }

    @Override
    public short commandId() {
        return this.header.commandId();
    }
}
