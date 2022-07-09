package com.example.gateway.server.message;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseMessage extends Message {
    private Header header;

    public Header header() {
        return this.header;
    }

    @Override
    public short commandId() {
        return this.header().commandId();
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return this.header.toString();
    }
}
