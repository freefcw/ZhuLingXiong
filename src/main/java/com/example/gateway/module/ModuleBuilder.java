package com.example.gateway.module;

import com.example.gateway.server.logic.GatewayModule;
import com.example.gateway.upstream.ConnectionManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ModuleBuilder {
    private final ConnectionManagerFactory connectionManagerFactory;

    public ModuleBuilder(ConnectionManagerFactory connectionManagerFactory) {
        this.connectionManagerFactory = connectionManagerFactory;
    }

    public GatewayModule buildWithProperty(ModuleProperty property) {
        ProxyModule module = new ProxyModule(property);
        module.setConnectionManager(this.connectionManagerFactory.buildConnectionManager(property.getType(), property.getIp(), property.getPort()));

        return module;
    }
}
