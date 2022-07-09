package com.example.gateway.action;

import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.session.NetSession;
import com.google.protobuf.InvalidProtocolBufferException;

public interface GatewayHandler {
    Boolean support(BaseMessage message);

    void handle(NetSession session, BaseMessage message) throws InvalidProtocolBufferException;
}
