package com.example.gateway.server.logic;

import com.example.gateway.module.ModuleProperty;
import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.session.NetSession;
import com.google.protobuf.InvalidProtocolBufferException;

public interface GatewayModule {

    String name();

    boolean support(short command);

    ModuleProperty getProperty();

    void shutdown();

    void handle(NetSession session, BaseMessage message) throws InvalidProtocolBufferException;
}
