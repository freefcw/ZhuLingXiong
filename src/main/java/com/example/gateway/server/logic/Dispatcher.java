package com.example.gateway.server.logic;


import com.example.gateway.module.ModuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Dispatcher {

    private final ModuleManager manager;

    public Dispatcher(ModuleManager manager) {
        this.manager = manager;
    }

    public GatewayModule getActionHandler(short commandId) {
        return this.manager.getModule(commandId);
    }
}
