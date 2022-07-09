package com.example.gateway.module;

import com.example.gateway.server.logic.GatewayModule;
import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.session.NetSession;
import com.example.gateway.upstream.common.ConnectionManager;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Slf4j
public class ProxyModule implements GatewayModule {
    private final ModuleProperty property;
    private ConnectionManager connectionManager;

    public ProxyModule(ModuleProperty property) {
        this.property = property;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public ModuleProperty getProperty() {
        return this.property;
    }

    @Override
    public void shutdown() {
        this.connectionManager.shutdown();
    }

    @Override
    public String name() {
        return this.property.getName();
    }

    @Override
    public boolean support(short command) {
        return this.property.support(command);
    }

    @Override
    public void handle(NetSession session, BaseMessage message) throws InvalidProtocolBufferException {
        try {
            this.connectionManager.writeAndFlush(session, message);
        } catch (InterruptedException e) {
            log.error("catch exception on write to remote: {}", e.getMessage());
            log.warn("user {} message will discard {}", session.userId(), message.commandId());
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("property", property)
                .toString();
    }
}
