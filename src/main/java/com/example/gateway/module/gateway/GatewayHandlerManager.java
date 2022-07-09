package com.example.gateway.module.gateway;

import com.example.gateway.action.GatewayHandler;
import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.session.NetSession;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class GatewayHandlerManager {
    private final List<GatewayHandler> handlerList;

    public GatewayHandlerManager(List<GatewayHandler> handlerList) {
        this.handlerList = handlerList;
    }

    public void handle(NetSession session, BaseMessage message) throws InvalidProtocolBufferException {
        for (GatewayHandler handler : this.handlerList) {
            if (handler.support(message)) {
                handler.handle(session, message);
            }
        }
    }
}
