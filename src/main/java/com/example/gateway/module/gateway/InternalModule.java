package com.example.gateway.module.gateway;

import com.example.gateway.module.ModuleProperty;
import com.example.gateway.module.ModuleType;
import com.example.gateway.server.logic.GatewayModule;
import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.session.NetSession;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InternalModule implements GatewayModule {

    private final GatewayHandlerManager gatewayHandlerManager;
    private final ModuleProperty property;

    public InternalModule(GatewayHandlerManager gatewayHandlerManager) {
        this.gatewayHandlerManager = gatewayHandlerManager;
        this.property = new ModuleProperty();
        property.setName("gateway");
        property.setType(ModuleType.INTERNAL);
        property.setCommandStart(0);
        property.setCommandEnd(99);
    }

    @Override
    public String name() {
        return "gateway";
    }

    @Override
    public boolean support(short command) {
        return command >= this.property.getCommandStart() && command < this.property.getCommandEnd();
    }

    @Override
    public ModuleProperty getProperty() {
        return this.property;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void handle(NetSession session, BaseMessage message) {
        log.debug("handle {}", message.commandId());
        try {
            this.gatewayHandlerManager.handle(session, message);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
