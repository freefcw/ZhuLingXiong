package com.example.gateway.action.heartbeat;

import com.example.gateway.action.GatewayHandler;
import com.example.gateway.module.gateway.GeneralActionType;
import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.session.NetSession;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HeartbeatHandler implements GatewayHandler, ApplicationContextAware {

    private ApplicationContext appContext;

    @Override
    public Boolean support(BaseMessage message) {
        return message.commandId() == GeneralActionType.HeartBeat.id();
    }

    @Override
    public void handle(NetSession session, BaseMessage message) throws InvalidProtocolBufferException {
        log.info("handle message {} from user {}", message.commandId(), session.userId());
        this.appContext.publishEvent(new HeartBeatEvent(this, session.userId()));
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }
}
